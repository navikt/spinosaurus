package no.nav.syfo.domain

/**
 * https://confluence.adeo.no/display/BOA/saf+-+Enums
 */
enum class JournalStatus {
    MOTTATT, // Tidligere: MIDLERTIDIG
    UKJENT, // Tidligere: ANNET
    FERDIGSTILT, // Tidligere: ENDELIG
    MIDLERTIDIG,
    JOURNALFOERT,
    FEILREGISTRERT,
}
