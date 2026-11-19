package no.nav.syfo.utsattoppgave

import no.nav.hag.utils.bakgrunnsjobb.Bakgrunnsjobb
import no.nav.hag.utils.bakgrunnsjobb.BakgrunnsjobbProsesserer
import no.nav.helsearbeidsgiver.utils.log.MdcUtils
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.syfo.util.UtsattOppgaveJsonMapper

/**
 * En bakgrunnsjobb som tar feilede meldinger ang utsatt oppgave og prøver å prosessere dem på nytt
 */

class FeiletUtsattOppgaveMeldingProsessor(
    val oppgaveService: UtsattOppgaveService,
) : BakgrunnsjobbProsesserer {
    private val logger = this.logger()

    override val type: String get() = JOB_TYPE

    companion object {
        const val JOB_TYPE = "feilet-utsatt-oppgave"
    }

    override fun prosesser(jobb: Bakgrunnsjobb) {
        val utsattOppgaveOppdatering = UtsattOppgaveJsonMapper.fromJson(jobb.data)
        val oppdatering =
            OppgaveOppdatering(
                utsattOppgaveOppdatering.dokumentId,
                utsattOppgaveOppdatering.oppdateringstype.tilHandling(),
                utsattOppgaveOppdatering.timeout,
                utsattOppgaveOppdatering.oppdateringstype,
            )

        MdcUtils.withCallId {
            logger.info("Prosesserer inntekstmelding " + oppdatering.id)
            oppgaveService.prosesser(oppdatering)
        }
    }
}
