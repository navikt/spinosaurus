package no.nav.syfo

import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger
import no.nav.syfo.integration.kafka.UtsattOppgaveConsumer
import no.nav.syfo.simba.InntektsmeldingConsumer
import no.nav.syfo.util.KubernetesProbeManager
import no.nav.syfo.web.inntektsmeldingModule
import no.nav.syfo.web.nais.nais
import org.flywaydb.core.Flyway
import javax.sql.DataSource
import kotlin.concurrent.thread

private val logger = "spinosaurus-main".logger()
private val sikkerlogger = sikkerLogger()

fun main() {
    val components = AppComponents()

    migrateDatabase(components.dataSource)
    components.leaderElectionBakgrunnsjobbProcessor.startAsync(true)
    startKafkaConsumer(components.inntektsmeldingConsumer, components.utsattOppgaveConsumer)

    val server =
        embeddedServer(
            factory = Netty,
            port = 8080,
            module = {
                nais()
                inntektsmeldingModule(components.inntektsmeldingService)
            },
        ).also {
            it.addShutdownHook {
                components.leaderElectionBakgrunnsjobbProcessor.stopAll()
            }
        }.start(wait = false)

    Thread.currentThread().setUncaughtExceptionHandler { thread, err ->
        "Uncaught exception in thread ${thread.name}: ${err.message}".also {
            logger.error(it)
            sikkerlogger.error(it, err)
        }
    }

    Runtime.getRuntime().addShutdownHook(
        Thread {
            logger.info("Fikk shutdown-signal, avslutter...")
            server.stop(1000, 1000)
            logger.info("Avsluttet OK")
        },
    )
}

private fun migrateDatabase(dataSource: DataSource) {
    logger.info("Starter databasemigrering")

    Flyway
        .configure()
        .baselineOnMigrate(true)
        .dataSource(dataSource)
        .load()
        .migrate()

    logger.info("Databasemigrering slutt")
}

private fun startKafkaConsumer(
    inntektsmeldingConsumer: InntektsmeldingConsumer,
    utsattOppgaveConsumer: UtsattOppgaveConsumer,
) {
    logger.info("Starter lytting for UtsattOppgave hendelser...")
    thread(start = true) {
        utsattOppgaveConsumer.start()
    }

    logger.info("Starter lytting for mottak fra simba...")
    logger.info("InntektsmeldingConsumer = $inntektsmeldingConsumer")
    thread(start = true) {
        logger.info("Starter inntektsmeldingConsumer-tråd")
        inntektsmeldingConsumer.start()
    }

    logger.info("Registrerer helsesjekker for kafka konsumentene...")
    val list = listOf(utsattOppgaveConsumer, inntektsmeldingConsumer)
    list.forEach {
        logger.info("Registrerer helsesjekker for ${it.javaClass}")
        KubernetesProbeManager.registerLivenessComponent(it)
        KubernetesProbeManager.registerReadynessComponent(it)
    }
}
