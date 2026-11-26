package no.nav.syfo

object Env {
    object Auth {
        val discoveryUrl = "AZURE_APP_WELL_KNOWN_URL".fromEnv()
        val acceptedAudience = "AZURE_APP_CLIENT_ID".fromEnv().let(::listOf)
        val preauthorizedApps = "AZURE_APP_PRE_AUTHORIZED_APPS".fromEnv()
    }

    object Database {
        val username = "NAIS_DATABASE_SPINOSAURUS_SPINOSAURUS_USERNAME".fromEnv()
        val password = "NAIS_DATABASE_SPINOSAURUS_SPINOSAURUS_PASSWORD".fromEnv()
        val url =
            String.format(
                "jdbc:postgresql://%s:%s/%s",
                "NAIS_DATABASE_SPINOSAURUS_SPINOSAURUS_HOST".fromEnv(),
                "NAIS_DATABASE_SPINOSAURUS_SPINOSAURUS_PORT".fromEnv(),
                "NAIS_DATABASE_SPINOSAURUS_SPINOSAURUS_DATABASE".fromEnv(),
            )
    }

    object Kafka {
        val topicInntektsmelding = "KAFKA_TOPIC_INNTEKTSMELDING".fromEnv()
        val topicUtsattOppgave = "KAFKA_TOPIC_UTSATT_OPPGAVE".fromEnv()

        val brokers = "KAFKA_BROKERS".fromEnv()
        val truststorePath = "KAFKA_TRUSTSTORE_PATH".fromEnv()
        val keystorePath = "KAFKA_KEYSTORE_PATH".fromEnv()
        val credstorePassword = "KAFKA_CREDSTORE_PASSWORD".fromEnv()
    }

    object Client {
        val naisTokenEndpoint = "NAIS_TOKEN_ENDPOINT".fromEnv()
        val urlElector = "ELECTOR_GET_URL".fromEnv()
        val urlNorg2 = "NORG2_URL".fromEnv()
        val urlOppgave = "OPPGAVEBEHANDLING_URL".fromEnv()
        val urlPdl = "PDL_URL".fromEnv()
        val scopeOppgave = "OPPGAVE_SCOPE".fromEnv()
        val scopePdl = "PDL_SCOPE".fromEnv()
    }
}

private fun String.fromEnv(): String =
    System.getenv(this)
        ?: throw IllegalStateException("Mangler miljøvariabel med nøkkel '$this'.")
