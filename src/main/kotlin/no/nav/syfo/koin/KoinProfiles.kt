package no.nav.syfo.koin

import io.ktor.server.config.ApplicationConfig
import no.nav.syfo.util.AppEnv.DEV
import no.nav.syfo.util.AppEnv.PROD
import no.nav.syfo.util.KubernetesProbeManager
import no.nav.syfo.util.createHttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

fun selectModuleBasedOnProfile(config: ApplicationConfig): List<Module> {
    val envModule =
        when (config.property("koin.profile").getString()) {
            PROD.name -> prodConfig(config)
            DEV.name -> devConfig(config)
            else -> localDevConfig(config)
        }
    return listOf(common, envModule)
}

val common =
    module {
        single { KubernetesProbeManager }

        single {
            createHttpClient(maxRetries = 3)
        }
    }

fun ApplicationConfig.getjdbcUrlFromProperties(): String =
    String.format(
        "jdbc:postgresql://%s:%s/%s",
        this.property("database.host").getString(),
        this.property("database.port").getString(),
        this.property("database.name").getString(),
    )
