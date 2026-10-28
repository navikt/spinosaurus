package no.nav.syfo.dto

import no.nav.helsearbeidsgiver.utils.wrapper.Fnr

data class InntektsmeldingEntitet(
    val uuid: String,
    var aktorId: String,
    val fnr: Fnr,
    var data: String,
)
