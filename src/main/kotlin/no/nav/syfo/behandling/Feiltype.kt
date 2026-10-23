package no.nav.syfo.behandling

enum class Feiltype(
    val navn: String,
) {
    AKTØR_IKKE_FUNNET("aktor_ikkefunnet"),
    OPPGAVE_HENT("oppgave_hent"),
    OPPGAVE_OPPRETT_VANLIG("oppgave_opprett"),
    BEHANDLENDE_INGEN_AKTIV_ENHET("behandlede_ingenaktiv"),
    BEHANDLENDE_FEILET("behandlede_feilet"),
}
