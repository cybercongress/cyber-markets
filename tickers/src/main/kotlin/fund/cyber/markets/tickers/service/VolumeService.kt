package fund.cyber.markets.tickers.service

import fund.cyber.markets.cassandra.repository.VolumeRepository
import fund.cyber.markets.common.Durations
import fund.cyber.markets.kafka.JsonDeserializer
import fund.cyber.markets.kafka.JsonSerializer
import fund.cyber.markets.model.TokenVolume
import fund.cyber.markets.model.TokenVolumeKey
import fund.cyber.markets.tickers.configuration.TickersConfiguration
import io.reactivex.schedulers.Schedulers
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory
import java.sql.Timestamp

class VolumeService(private val configuration: TickersConfiguration,
                    private val volumeRepository: VolumeRepository) {

    private val log = LoggerFactory.getLogger(VolumeService::class.java)!!

    private val consumer = KafkaConsumer<TokenVolumeKey, TokenVolume>(
            configuration.volumeBackupConsumerConfig,
            JsonDeserializer(TokenVolumeKey::class.java),
            JsonDeserializer(TokenVolume::class.java)
    )

    private val producer = KafkaProducer<TokenVolumeKey, TokenVolume>(
            configuration.volumeProducerConfig,
            JsonSerializer<TokenVolumeKey>(),
            JsonSerializer<TokenVolume>()
    ).apply { initTransactions() }

    fun saveAndProduceToKafka(volumes: MutableMap<String, MutableMap<String, MutableMap<Long, TokenVolume>>>, currentMillisHop: Long) {
        val volumeSnapshots = mutableListOf<TokenVolume>()
        val topicName = configuration.volumesTopicName

        producer.beginTransaction()
        try {
            volumes.forEach { _, exchangeMap ->
                exchangeMap.forEach { _, windowDurMap ->
                    windowDurMap.forEach { _, volume ->
                        if (configuration.allowNotClosedWindows) {
                            producer.send(produceRecord(volume, topicName))
                        } else if (volume.timestampTo.time <= currentMillisHop) {
                            producer.send(produceRecord(volume, topicName))
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
            snapshots.forEach { volume ->
                try {
                    volumeRepository.save(volume)
                } catch (e: Exception) {
                    log.error("Cannot save volume to cassandra", e)
                    backupToKafka(volume)
                }
            }
        }

        restoreFromKafka()
    }

    private fun backupToKafka(volume: TokenVolume) {
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

            records.forEach { record ->
                try {
                    volumeRepository.save(record.value())
                } catch (e: Exception) {
                    log.debug("Restore failed: {} ", record.value())
                }
            }
        }
    }

    private fun isSnapshot(volume: TokenVolume): Boolean {
        return volume.timestampTo.time % Durations.MINUTE == 0L
    }

    private fun produceRecord(volume: TokenVolume, topicName: String): ProducerRecord<TokenVolumeKey, TokenVolume> {
        return ProducerRecord(
                topicName,
                TokenVolumeKey(volume.token, volume.windowDuration, Timestamp(volume.timestampTo.time)),
                volume)
    }

}