package no.nav.syfo.util

import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

fun kafkaConsumerTestProps(): Map<String, Any> =
    mapOf(
        CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG to "localhost:9092",
        ConsumerConfig.MAX_POLL_RECORDS_CONFIG to "1",
        ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG to "30000",
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
        ConsumerConfig.GROUP_ID_CONFIG to "syfoinntektsmelding-v2",
        ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
    )
