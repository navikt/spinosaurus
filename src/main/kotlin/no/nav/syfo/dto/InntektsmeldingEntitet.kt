package no.nav.syfo.dto

import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import java.util.UUID

data class InntektsmeldingEntitet(
    val uuid: UUID,
    var aktorId: String,
    val fnr: Fnr,
    var data: String,
)
