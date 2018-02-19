package fund.cyber.markets.ticker.service

import fund.cyber.markets.cassandra.model.CqlTokenVolume
import fund.cyber.markets.cassandra.repository.VolumeRepository
import fund.cyber.markets.common.Durations
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.kafka.JsonSerializer
import fund.cyber.markets.model.TokenVolume
import fund.cyber.markets.model.TokenVolumeKey
import fund.cyber.markets.ticker.configuration.TickersConfiguration
import io.reactivex.schedulers.Schedulers
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.sql.Timestamp

@Service
class VolumeService {

    private val log = LoggerFactory.getLogger(VolumeService::class.java)!!

    @Autowired
    lateinit var configuration: TickersConfiguration

    @Autowired
    lateinit var volumeRepository: VolumeRepository

    private val consumer by lazy { KafkaConsumer<TokenVolumeKey, TokenVolume>(
            configuration.volumeBackupConsumerConfig,
            JsonDeserializer(TokenVolumeKey::class.java),
            JsonDeserializer(TokenVolume::class.java)
    )}

    private val producer by lazy { KafkaProducer<TokenVolumeKey, CqlTokenVolume>(
            configuration.volumeProducerConfig,
            JsonSerializer<TokenVolumeKey>(),
            JsonSerializer<CqlTokenVolume>()
    ).apply { initTransactions() } }

    fun saveAndProduceToKafka(volumes: MutableMap<String, MutableMap<String, MutableMap<Long, TokenVolume>>>, currentMillisHop: Long) {
        val volumeSnapshots = mutableListOf<TokenVolume>()
        val topicName = configuration.volumesTopicName

        producer.beginTransaction()
        try {
            volumes.forEach { _, exchangeMap ->
                exchangeMap.forEach { _, windowDurMap ->
                    windowDurMap.forEach { _, volume ->
                        if (configuration.allowNotClosedWindows) {
                            producer.send(produceRecord(CqlTokenVolume(volume), topicName))
                        } else if (volume.timestampTo.time <= currentMillisHop) {
                            producer.send(produceRecord(CqlTokenVolume(volume), topicName))
                        }
                        if (volume.timestampTo.time <= currentMillisHop && isSnapshot(volume)) {
                            volumeSnapshots.add(volume)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            log.error("Cannot produce volume to kafka", e)
            producer.abortTransaction()
            Runtime.getRuntime().exit(-1)
        }
        producer.commitTransaction()

        if (volumeSnapshots.isNotEmpty()) {
            saveSnapshots(volumeSnapshots)
        }
    }

    private fun saveSnapshots(volumeSnapshots: MutableList<TokenVolume>) {
        log.debug("Save volume snapshots")

        val snapshots = mutableListOf<TokenVolume>()
        volumeSnapshots.forEach {
            snapshots.add(it.copy())
        }

        Schedulers.io().scheduleDirect {
            snapshots
                    .map { volume ->
                        CqlTokenVolume(volume)
                    }
                    .forEach { volume ->
                        try {
                            volumeRepository.save(volume).block()
                        } catch (e: Exception) {
                            log.error("Cannot save volume to cassandra", e)
                            backupToKafka(volume)
                        }
                    }
        }

        restoreFromKafka()
    }

    private fun backupToKafka(volume: CqlTokenVolume) {
        log.debug("Backuping volume to kafka: {}", volume)

        producer.beginTransaction()
        try {
            producer.send(produceRecord(volume, configuration.volumesBackupTopicName))
        } catch (e: Exception) {
            log.error("Cannot backup volume to kafka", e)
            producer.abortTransaction()
            Runtime.getRuntime().exit(-1)
        }
        producer.commitTransaction()
    }

    private fun restoreFromKafka() {
        log.debug("Restore volumes from kafka")

        Schedulers.single().scheduleDirect {
            val records = consumer.poll(configuration.pollTimeout)
            log.debug("Volumes for restore count: {}", records.count())

            records
                    .map { volumeRecord ->
                        CqlTokenVolume(volumeRecord.value())
                    }
                    .forEach { cqlTokenVolume ->
                        try {
                            volumeRepository.save(cqlTokenVolume).block()
                        } catch (e: Exception) {
                            log.debug("Restore failed: {} ", cqlTokenVolume)
                        }
                    }
        }
    }

    private fun isSnapshot(volume: TokenVolume): Boolean {
        return volume.timestampTo.time % Durations.MINUTE == 0L
    }

    private fun produceRecord(volume: CqlTokenVolume, topicName: String): ProducerRecord<TokenVolumeKey, CqlTokenVolume> {
        return ProducerRecord(
                topicName,
                TokenVolumeKey(volume.token, volume.windowDuration, Timestamp(volume.timestampTo.time)),
                volume)
    }

}