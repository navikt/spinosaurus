package no.nav.syfo.dto

import java.time.LocalDate
import java.util.UUID

data class ArbeidsgiverperiodeEntitet(
    val uuid: String = UUID.randomUUID().toString(),
    var inntektsmelding: InntektsmeldingEntitet? = null,
    val fom: LocalDate,
    val tom: LocalDate,
    val inntektsmelding_uuid: String = "",
)
