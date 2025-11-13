package no.nav.syfo.koin

import io.ktor.server.config.ApplicationConfig
import io.mockk.every
import io.mockk.mockk
import no.nav.helsearbeidsgiver.pdl.PdlClient
import no.nav.syfo.client.dokarkiv.DokArkivClient
import no.nav.syfo.client.norg.Norg2Client
import no.nav.syfo.client.oppgave.OppgaveService
import no.nav.syfo.client.saf.SafDokumentClient
import no.nav.syfo.repository.InntektsmeldingRepository
import no.nav.syfo.util.AppEnv
import no.nav.syfo.util.Metrikk
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import javax.sql.DataSource
import kotlin.test.assertNotNull

class KoinProfilesKtTest : KoinTest {
    val config = mockk<ApplicationConfig>(relaxed = true)
    val clientConfig = mockk<ApplicationConfig>(relaxed = true)
    val metrikk = mockk<Metrikk>(relaxed = true)

    val dataSource = mockk<DataSource>(relaxed = true)
    val inntektsmeldingRepository: InntektsmeldingRepository by inject()
    val pdlClient: PdlClient by inject()
    val safDokumentClient: SafDokumentClient by inject()
    val safJournalpostClient: SafDokumentClient by inject()
    val dokArkivClient: DokArkivClient by inject()
    val norg2Client: Norg2Client by inject()
    val oppgaveService: OppgaveService by inject()

    @Test
    fun `test prodConfig`() {
        mockConfig(AppEnv.PROD)

        startKoin {
            modules(selectModuleBasedOnProfile(config) + getTestModules())
        }

        assertKoin()
        stopKoin()
    }

    @Test
    fun `test devConfig`() {
        mockConfig(AppEnv.DEV)

        startKoin {
            modules(selectModuleBasedOnProfile(config) + getTestModules())
        }

        assertKoin()
        stopKoin()
    }

    @Test
    fun `test localConfig`() {
        mockConfig(AppEnv.LOCAL)

        startKoin {
            modules(selectModuleBasedOnProfile(config) + getTestModules())
        }

        assertKoin()
        stopKoin()
    }

    private fun assertKoin() {
        assertNotNull(dataSource)
        assertNotNull(inntektsmeldingRepository)
        assertNotNull(pdlClient)
        assertNotNull(safDokumentClient)
        assertNotNull(dokArkivClient)
        assertNotNull(norg2Client)
        assertNotNull(oppgaveService)
        assertNotNull(safJournalpostClient)
    }

    private fun getTestModules(): Module {
        val testModule =
            module {
                single { dataSource } bind DataSource::class

                single { metrikk }
            }
        return testModule
    }

    private fun mockConfig(appEnv: AppEnv) {
        every { config.property("koin.profile").getString() } returns appEnv.name
        every { config.configList("no.nav.security.jwt.client.registration.clients") } returns listOf(clientConfig)
        every { config.property("saf_dokument_url").getString() } returns "saf_url"
        every { config.property("dokarkiv_url").getString() } returns "dokarkiv_url"
    }
}
