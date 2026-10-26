package no.nav.syfo.util

import no.nav.syfo.integration.kafka.consumerLocalProperties
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

fun kafkaConsumerTestProps() =
    consumerLocalProperties() +
        mapOf(
            ConsumerConfig.GROUP_ID_CONFIG to "syfoinntektsmelding-v2",
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        )
