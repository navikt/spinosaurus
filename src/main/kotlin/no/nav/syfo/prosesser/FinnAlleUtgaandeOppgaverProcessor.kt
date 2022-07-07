package no.nav.syfo.prosesser

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import no.nav.helse.arbeidsgiver.utils.RecurringJob
import no.nav.helse.arbeidsgiver.utils.logger
import no.nav.syfo.behandling.OpprettOppgaveException
import no.nav.syfo.dto.Tilstand
import no.nav.syfo.repository.InntektsmeldingRepository
import no.nav.syfo.util.MDCOperations
import no.nav.syfo.util.Metrikk
import no.nav.syfo.utsattoppgave.UtsattOppgaveDAO
import no.nav.syfo.utsattoppgave.UtsattOppgaveService
import java.time.Duration
import java.time.LocalDateTime
import java.util.UUID

class FinnAlleUtgaandeOppgaverProcessor(
    private val utsattOppgaveService: UtsattOppgaveService,
    private val utsattOppgaveDAO: UtsattOppgaveDAO,
    private val metrikk: Metrikk,
    private val inntektsmeldingRepository: InntektsmeldingRepository,
) : RecurringJob(CoroutineScope(Dispatchers.IO), Duration.ofHours(6).toMillis()) {
    private val logger = this.logger()

    override fun doJob() {
        MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, UUID.randomUUID().toString())
        utsattOppgaveDAO
            .finnAlleUtgåtteOppgaver()
            .forEach {
                try {
                    logger.info("Skal opprette oppgave for inntektsmelding: ${it.arkivreferanse}")
                    val inntektsmeldingEntitet = inntektsmeldingRepository.findByArkivReferanse(it.arkivreferanse)
                    utsattOppgaveService.opprettOppgaveIGosys(it, it.speil, inntektsmeldingEntitet)
                    it.tilstand = Tilstand.OpprettetTimeout
                    it.oppdatert = LocalDateTime.now()
                    metrikk.tellUtsattOppgave_OpprettTimeout()
                    utsattOppgaveDAO.lagre(it)
                    logger.info("Oppgave opprettet i gosys pga timeout for inntektsmelding: ${it.arkivreferanse}")
                } catch (e: OpprettOppgaveException) {
                    logger.error("Feilet ved opprettelse av oppgave ved timeout i gosys for inntektsmelding: ${it.arkivreferanse}")
                }
            }
        MDCOperations.remove(MDCOperations.MDC_CALL_ID)
    }
}
