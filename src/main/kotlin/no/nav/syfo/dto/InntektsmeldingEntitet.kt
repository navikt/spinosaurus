package no.nav.syfo.dto

import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import java.time.LocalDateTime

data class InntektsmeldingEntitet(
    val uuid: String,
    var aktorId: String,
    val journalpostId: String,
    val orgnummer: String? = null,
    val arbeidsgiverPrivat: String? = null,
    val behandlet: LocalDateTime? = LocalDateTime.now(),
    val fnr: Fnr,
    var data: String? = null,
)
