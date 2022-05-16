package no.nav.syfo.domain

data class InngaendeJournalpost(
    val fnr: String,
    val journalpostId: String,
    val dokumentId: String,
    val behandlendeEnhetId: String,
    val arbeidsgiverOrgnummer: String? = null,
    val arbeidsgiverNavn: String = "Arbeidsgiver",
    val arbeidsgiverPrivat: String? = null
)
