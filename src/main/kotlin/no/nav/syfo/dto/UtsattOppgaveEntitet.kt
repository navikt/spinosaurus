package no.nav.syfo.dto

import java.time.LocalDateTime

data class UtsattOppgaveEntitet(
    val id: Int = 0,
    val inntektsmeldingId: String,
    val arkivreferanse: String,
    val fnr: String,
    val aktørId: String,
    val journalpostId: String,
    val timeout: LocalDateTime,
    val tilstand: Tilstand,
    val enhet: String = "",
    val gosysOppgaveId: String?,
    val oppdatert: LocalDateTime?,
    val speil: Boolean,
    val utbetalingBruker: Boolean,
)

enum class Tilstand {
    Utsatt,
    Forkastet,
    Opprettet,
    OpprettetTimeout,
}
