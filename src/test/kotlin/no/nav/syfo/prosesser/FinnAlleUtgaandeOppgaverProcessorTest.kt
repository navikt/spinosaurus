package no.nav.syfo.prosesser

import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import no.nav.syfo.UtsattOppgaveTestData
import no.nav.syfo.client.oppgave.OppgaveService
import no.nav.syfo.domain.OppgaveResultat
import no.nav.syfo.dto.Tilstand
import no.nav.syfo.isEqualNullSafe
import no.nav.syfo.koin.buildObjectMapper
import no.nav.syfo.repository.InntektsmeldingRepository
import no.nav.syfo.service.BehandlendeEnhetConsumer
import no.nav.syfo.utsattoppgave.UtsattOppgaveDAO
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.random.Random

class FinnAlleUtgaandeOppgaverProcessorTest {
    val utsattOppgaveDAO: UtsattOppgaveDAO = mockk(relaxed = true)
    val oppgaveService: OppgaveService = mockk(relaxed = true)
    val behandlendeEnhetConsumer: BehandlendeEnhetConsumer = mockk(relaxed = true)
    val inntektsmeldingRepository: InntektsmeldingRepository = mockk(relaxed = true)

    val processor =
        FinnAlleUtgaandeOppgaverProcessor(
            utsattOppgaveDAO = utsattOppgaveDAO,
            oppgaveService = oppgaveService,
            behandlendeEnhetConsumer = behandlendeEnhetConsumer,
            metrikk = mockk(relaxed = true),
            inntektsmeldingRepository = inntektsmeldingRepository,
            om = buildObjectMapper(),
        )

    val oppgave = UtsattOppgaveTestData.oppgave
    val timeout = LocalDateTime.of(2023, 4, 6, 9, 0)

    @BeforeEach
    fun setup() {
        clearAllMocks()
        every { utsattOppgaveDAO.finnAlleUtgaatteOppgaver() } returns listOf(oppgave)
        coEvery { oppgaveService.opprettOppgave(any(), any(), any()) } returns OppgaveResultat(Random.nextInt(), false, false)
        every { behandlendeEnhetConsumer.hentBehandlendeEnhet(any(), any()) } returns "4488"
        every { inntektsmeldingRepository.findByUuid(any()) } returns UtsattOppgaveTestData.inntektsmeldingEntitet
    }

    @Test
    fun `Oppretter oppgave ved timout og lagrer tilstand OpprettetTimeout`() {
        processor.doJob()
        verify {
            utsattOppgaveDAO.oppdater(
                match { it.tilstand == Tilstand.OpprettetTimeout && !it.speil && it.timeout.isEqual(timeout) && !it.oppdatert.isEqualNullSafe(oppgave.oppdatert) },
            )
        }
        coVerify { oppgaveService.opprettOppgave(any(), any(), any()) }
    }

    @Test
    fun `Oppretter ikke oppgave ved timeout hvis begrunnelseRedusert = IkkeFravaer`() {
        every { inntektsmeldingRepository.findByUuid(any()) } returns UtsattOppgaveTestData.inntektsmeldingEntitetIkkeFravaer
        processor.doJob()
        verify {
            utsattOppgaveDAO.oppdater(
                match { it.tilstand == Tilstand.Forkastet && !it.speil && it.timeout.isEqual(timeout) && !it.oppdatert.isEqualNullSafe(oppgave.oppdatert) },
            )
        }
        coVerify(exactly = 0) { oppgaveService.opprettOppgave(any(), any(), any()) }
    }
}
