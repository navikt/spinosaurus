package no.nav.syfo.utsattoppgave

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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

class UtsattOppgaveServiceTest {
    val utsattOppgaveDAO: UtsattOppgaveDAO = mockk(relaxed = true)
    val oppgaveService: OppgaveService = mockk(relaxed = true)
    val behandlendeEnhetConsumer: BehandlendeEnhetConsumer = mockk(relaxed = true)
    val inntektsmeldingRepository: InntektsmeldingRepository = mockk(relaxed = true)

    val utsattOppgaveService =
        UtsattOppgaveService(
            utsattOppgaveDAO = utsattOppgaveDAO,
            oppgaveService = oppgaveService,
            behandlendeEnhetConsumer = behandlendeEnhetConsumer,
            inntektsmeldingRepository = inntektsmeldingRepository,
            om = buildObjectMapper(),
            metrikk = mockk(relaxed = true),
        )

    val oppgave = UtsattOppgaveTestData.oppgave
    val timeout = LocalDateTime.of(2023, 4, 6, 9, 0)

    @BeforeEach
    fun setup() {
        clearAllMocks()
        every { utsattOppgaveDAO.finn(any()) } returns oppgave
        coEvery { oppgaveService.opprettOppgave(any(), any(), any()) } returns OppgaveResultat(Random.nextInt(), false, false)
        every { behandlendeEnhetConsumer.hentBehandlendeEnhet(any(), any()) } returns "4488"
        every { inntektsmeldingRepository.findByUuid(any()) } returns UtsattOppgaveTestData.inntektsmeldingEntitet
    }

    @Test
    fun `Oppretter forsinket oppgave med timeout`() {
        utsattOppgaveService.opprett(oppgave)
        verify { utsattOppgaveDAO.opprett(oppgave) }
    }

    @Test
    fun `Lagrer utsatt oppgave med gjelder speil flagg og tilstand Opprettet for OpprettSpeilRelatert`() {
        val oppgaveOppdatering =
            OppgaveOppdatering(
                UUID.randomUUID(),
                OppdateringstypeDTO.OpprettSpeilRelatert.tilHandling(),
                timeout.plusDays(7),
                OppdateringstypeDTO.OpprettSpeilRelatert,
            )
        utsattOppgaveService.prosesser(oppgaveOppdatering)
        verify { utsattOppgaveDAO.oppdater(match { it.tilstand == Tilstand.Opprettet && it.speil && it.timeout.isEqual(timeout) }) }
        coVerify { oppgaveService.opprettOppgave(any(), any(), any()) }
    }

    @Test
    fun `Lagrer utsatt oppgave med tilstand Opprettet for Opprett`() {
        val oppgaveOppdatering =
            OppgaveOppdatering(
                UUID.randomUUID(),
                OppdateringstypeDTO.Opprett.tilHandling(),
                timeout.plusDays(7),
                OppdateringstypeDTO.Opprett,
            )
        utsattOppgaveService.prosesser(oppgaveOppdatering)
        verify { utsattOppgaveDAO.oppdater(match { it.tilstand == Tilstand.Opprettet && !it.speil && it.timeout.isEqual(timeout) }) }
        coVerify { oppgaveService.opprettOppgave(any(), any(), any()) }
    }

    @Test
    fun `Lagrer utsatt oppgave med ny timeout for utsettelse`() {
        val nyTimeout = timeout.plusDays(7)
        val oppgaveOppdatering =
            OppgaveOppdatering(
                UUID.randomUUID(),
                OppdateringstypeDTO.Utsett.tilHandling(),
                nyTimeout,
                OppdateringstypeDTO.Utsett,
            )
        utsattOppgaveService.prosesser(oppgaveOppdatering)
        verify {
            utsattOppgaveDAO.oppdater(
                match { it.tilstand == Tilstand.Utsatt && it.timeout.isEqual(nyTimeout) && !it.oppdatert.isEqualNullSafe(oppgave.oppdatert) },
            )
        }
        coVerify(exactly = 0) { oppgaveService.opprettOppgave(any(), any(), any()) }
    }

    @Test
    fun `Lagrer utsatt oppgave med tilstand Forkastet ved Ferdigbehandlet`() {
        val oppgaveOppdatering =
            OppgaveOppdatering(
                UUID.randomUUID(),
                OppdateringstypeDTO.Ferdigbehandlet.tilHandling(),
                timeout.plusDays(7),
                OppdateringstypeDTO.Ferdigbehandlet,
            )
        utsattOppgaveService.prosesser(oppgaveOppdatering)
        verify {
            utsattOppgaveDAO.oppdater(
                match { it.tilstand == Tilstand.Forkastet && it.timeout.isEqual(timeout) && !it.oppdatert.isEqualNullSafe(oppgave.oppdatert) },
            )
        }
        coVerify(exactly = 0) { oppgaveService.opprettOppgave(any(), any(), any()) }
    }

    @Test
    fun `Oppretter Ikke Oppgave hvis begrunnelseRedusert = IkkeFravaer hvis oppgave utsatt`() {
        every { inntektsmeldingRepository.findByUuid(any()) } returns UtsattOppgaveTestData.inntektsmeldingEntitetIkkeFravaer
        val oppgaveOppdatering =
            OppgaveOppdatering(
                UUID.randomUUID(),
                OppdateringstypeDTO.Opprett.tilHandling(),
                timeout.plusDays(7),
                OppdateringstypeDTO.Opprett,
            )
        utsattOppgaveService.prosesser(oppgaveOppdatering)
        verify { utsattOppgaveDAO.oppdater(match { it.tilstand == Tilstand.Forkastet && !it.oppdatert.isEqualNullSafe(oppgave.oppdatert) }) }
        coVerify(exactly = 0) { oppgaveService.opprettOppgave(any(), any(), any()) }
    }

    @Test
    fun `Oppretter Ikke Oppgave hvis begrunnelseRedusert = IkkeFravaer og oppgave allerede forkastet`() {
        every { inntektsmeldingRepository.findByUuid(any()) } returns UtsattOppgaveTestData.inntektsmeldingEntitetIkkeFravaer
        val forkastetTidspunkt = LocalDateTime.of(2023, 4, 6, 9, 0)
        val forkastetOppgave = oppgave.copy(tilstand = Tilstand.Forkastet, oppdatert = forkastetTidspunkt)
        every { utsattOppgaveDAO.finn(any()) } returns forkastetOppgave
        val oppgaveOppdatering =
            OppgaveOppdatering(
                UUID.randomUUID(),
                OppdateringstypeDTO.Opprett.tilHandling(),
                timeout.plusDays(7),
                OppdateringstypeDTO.Opprett,
            )
        utsattOppgaveService.prosesser(oppgaveOppdatering)
        verify(exactly = 0) { utsattOppgaveDAO.oppdater(any()) }
        coVerify(exactly = 0) { oppgaveService.opprettOppgave(any(), any(), any()) }
    }
}
