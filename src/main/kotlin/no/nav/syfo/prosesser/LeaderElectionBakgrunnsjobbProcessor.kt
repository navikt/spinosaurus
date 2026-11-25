package no.nav.syfo.prosesser

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import no.nav.hag.utils.bakgrunnsjobb.BakgrunnsjobbService
import no.nav.hag.utils.bakgrunnsjobb.RecurringJob
import no.nav.syfo.util.LeaderElectionManager
import no.nav.syfo.utsattoppgave.FeiletUtsattOppgaveMeldingProsessor
import java.time.Duration

class LeaderElectionBakgrunnsjobbProcessor(
    val leaderElectionManager: LeaderElectionManager,
    val bakgrunnsjobbService: BakgrunnsjobbService,
    val finnAlleUtgaandeOppgaverProcessor: FinnAlleUtgaandeOppgaverProcessor,
    val feiletUtsattOppgaveMeldingProsessor: FeiletUtsattOppgaveMeldingProsessor,
) : RecurringJob(CoroutineScope(Dispatchers.IO), Duration.ofMinutes(5).toMillis()) {
    var started = false

    override fun doJob() {
        if (leaderElectionManager.isLeader() && !started) {
            started = true
            finnAlleUtgaandeOppgaverProcessor.startAsync(true)
            bakgrunnsjobbService.apply {
                registrer(feiletUtsattOppgaveMeldingProsessor)
                startAsync(true)
            }
        }
    }

    fun stopAll() {
        stop()
        finnAlleUtgaandeOppgaverProcessor.stop()

        // Et forsøk på at bakgrunnsjobber skal kunne ferdigstilles OK, før stopp. Ingen garantier.
        bakgrunnsjobbService.stop()
        logger.info("Venter ${bakgrunnsjobbService.delayMillis} ms på at bakgrunnsjobbService stoppes!")
        Thread.sleep(bakgrunnsjobbService.delayMillis)
    }
}
