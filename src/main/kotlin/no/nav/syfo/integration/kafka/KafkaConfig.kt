package no.nav.syfo.integration.kafka

import no.nav.syfo.Env
import org.apache.kafka.clients.CommonClientConfigs
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.config.SslConfigs
import org.apache.kafka.common.security.auth.SecurityProtocol
import org.apache.kafka.common.serialization.StringDeserializer

fun inntektsmeldingAivenProps(): Map<String, Any> =
    commonAivenProps() +
        mapOf(
            // viktig!
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to "false",
            // også viktig - unngår at vi commiter for stort offset hvis vi henter flere og feiler midt i batchen
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to "1",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.CLIENT_ID_CONFIG to "syfoinntektsmelding-im-consumer",
            ConsumerConfig.GROUP_ID_CONFIG to "syfoinntektsmelding-im-v1",
            ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
            ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG to StringDeserializer::class.java,
        )

fun utsattOppgaveAivenProps(): Map<String, Any> =
    commonAivenProps() +
        mapOf(
            ConsumerConfig.MAX_POLL_RECORDS_CONFIG to "1",
            ConsumerConfig.AUTO_OFFSET_RESET_CONFIG to "earliest",
            ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG to false,
            ConsumerConfig.GROUP_ID_CONFIG to "syfoinntektsmelding-v1",
        )

fun commonAivenProps(): Map<String, Any> =
    mapOf(
        ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG to "true",
        ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION to "1",
        ProducerConfig.MAX_BLOCK_MS_CONFIG to "15000",
        ProducerConfig.RETRIES_CONFIG to "2",
        ProducerConfig.ACKS_CONFIG to "all",
        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to Env.Kafka.brokers,
        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to "org.apache.kafka.common.serialization.StringSerializer",
        CommonClientConfigs.SECURITY_PROTOCOL_CONFIG to SecurityProtocol.SSL.name,
        SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG to "",
        SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG to "jks",
        SslConfigs.SSL_KEYSTORE_TYPE_CONFIG to "PKCS12",
        SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG to Env.Kafka.truststorePath,
        SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG to Env.Kafka.credstorePassword,
        SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG to Env.Kafka.keystorePath,
        SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG to Env.Kafka.credstorePassword,
        SslConfigs.SSL_KEY_PASSWORD_CONFIG to Env.Kafka.credstorePassword,
    )
