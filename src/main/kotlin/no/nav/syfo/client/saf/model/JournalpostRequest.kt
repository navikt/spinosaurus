package no.nav.syfo.client.saf.model

data class GetJournalpostRequest(
    val query: String,
    val variables: GetJournalpostVariables? = null
)

data class GetJournalpostVariables(
    val id: String
)
