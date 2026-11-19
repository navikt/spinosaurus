package no.nav.syfo.koin

import io.ktor.server.config.ApplicationConfig
import io.mockk.every
import io.mockk.mockk
import no.nav.helsearbeidsgiver.pdl.PdlClient
import no.nav.syfo.client.norg.Norg2Client
import no.nav.syfo.client.oppgave.OppgaveService
import no.nav.syfo.util.AppEnv
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.inject
import kotlin.test.assertNotNull

class KoinProfilesKtTest : KoinTest {
    val config = mockk<ApplicationConfig>(relaxed = true)
    val clientConfig = mockk<ApplicationConfig>(relaxed = true)

    val pdlClient: PdlClient by inject()
    val norg2Client: Norg2Client by inject()
    val oppgaveService: OppgaveService by inject()

    @Test
    fun `test prodConfig`() {
        mockConfig(AppEnv.PROD)

        startKoin {
            modules(selectModuleBasedOnProfile(config))
        }

        assertKoin()
        stopKoin()
    }

    @Test
    fun `test devConfig`() {
        mockConfig(AppEnv.DEV)

        startKoin {
            modules(selectModuleBasedOnProfile(config))
        }

        assertKoin()
        stopKoin()
    }

    @Test
    fun `test localConfig`() {
        mockConfig(AppEnv.LOCAL)

        startKoin {
            modules(selectModuleBasedOnProfile(config))
        }

        assertKoin()
        stopKoin()
    }

    private fun assertKoin() {
        assertNotNull(pdlClient)
        assertNotNull(norg2Client)
        assertNotNull(oppgaveService)
    }

    private fun mockConfig(appEnv: AppEnv) {
        every { config.property("koin.profile").getString() } returns appEnv.name
        every { config.configList("no.nav.security.jwt.client.registration.clients") } returns listOf(clientConfig)
    }
}
