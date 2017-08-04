package fund.cyber.markets.connectors.common.kafka

import fund.cyber.markets.kafka.JsonSerializer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.common.serialization.StringSerializer
import java.util.*

private val kafkaProps: Properties = Properties().apply {
    //Assign localhost id
    put("bootstrap.servers", "localhost:9092");

    //Set acknowledgements for producer requests.
    put("acks", "all");

    //If the request fails, the producer can automatically retry,
    put("retries", 0);

    //Specify buffer size in config
    put("batch.size", 16384);

    //Reduce the no of requests less than 0
    put("linger.ms", 1);

    //The buffer.memory controls the total amount of memory available to the producer for buffering.
    put("buffer.memory", 33554432);
}

class ConnectorKafkaProducer<T>(
        props: Properties = kafkaProps,
        keySerializer: StringSerializer = StringSerializer(),
        valueSerializer: JsonSerializer<T> = JsonSerializer<T>()
): KafkaProducer<String, T>(props, keySerializer, valueSerializer)