package no.nav.syfo.utsattoppgave

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.Bakgrunnsjobb
import no.nav.helse.arbeidsgiver.bakgrunnsjobb.BakgrunnsjobbProsesserer
import no.nav.syfo.util.MDCOperations
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * En bakgrunnsjobb som tar feilede meldinger ang utsatt oppgave og prøver å prosessere dem på nytt
 */

class FeiletUtsattOppgaveMeldingProsessor(
    private val om: ObjectMapper,
    val oppgaveService: UtsattOppgaveService
) :
    BakgrunnsjobbProsesserer {
    val log: Logger = LoggerFactory.getLogger(FeiletUtsattOppgaveMeldingProsessor::class.java)
    override val type: String get() = JOB_TYPE
    companion object {
        const val JOB_TYPE = "feilet-utsatt-oppgave"
    }
    override fun prosesser(jobb: Bakgrunnsjobb) {
        try {
            val utsattOppgaveOppdatering = om.readValue<UtsattOppgaveDTO>(jobb.data)
            val oppdatering = OppgaveOppdatering(
                utsattOppgaveOppdatering.dokumentId,
                utsattOppgaveOppdatering.oppdateringstype.tilHandling(),
                utsattOppgaveOppdatering.timeout,
                utsattOppgaveOppdatering.oppdateringstype
            )

            MDCOperations.putToMDC(MDCOperations.MDC_CALL_ID, MDCOperations.generateCallId())
            log.info("Prosesserer inntekstmelding " + oppdatering.id)
            oppgaveService.prosesser(oppdatering)
        } finally {
            MDCOperations.remove(MDCOperations.MDC_CALL_ID)
        }
    }
}
