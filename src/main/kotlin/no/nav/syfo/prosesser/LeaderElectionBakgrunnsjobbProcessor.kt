package no.nav.syfo.prosesser

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import no.nav.hag.utils.bakgrunnsjobb.BakgrunnsjobbService
import no.nav.hag.utils.bakgrunnsjobb.RecurringJob
import no.nav.syfo.util.LeaderElectionManager
import no.nav.syfo.utsattoppgave.FeiletUtsattOppgaveMeldingProsessor
import java.time.Duration

class LeaderElectionBakgrunnsjobbProcessor(
    val bakgrunnsjobbService: BakgrunnsjobbService,
    val finnAlleUtgaandeOppgaverProcessor: FinnAlleUtgaandeOppgaverProcessor,
    val fjernInntektsmeldingByBehandletProcessor: FjernInntektsmeldingByBehandletProcessor,
    val joarkInntektsmeldingHendelseProsessor: JoarkInntektsmeldingHendelseProsessor,
    val feiletUtsattOppgaveMeldingProsessor: FeiletUtsattOppgaveMeldingProsessor,
) : RecurringJob(CoroutineScope(Dispatchers.IO), Duration.ofMinutes(5).toMillis()) {
    var started = false

    override fun doJob() {
        if (LeaderElectionManager.isLeader() && !started) {
            started = true
            finnAlleUtgaandeOppgaverProcessor.startAsync(true)
            bakgrunnsjobbService.apply {
                registrer(feiletUtsattOppgaveMeldingProsessor)
                registrer(fjernInntektsmeldingByBehandletProcessor)
                registrer(joarkInntektsmeldingHendelseProsessor)
                startAsync(true)
            }
        }
    }
}
