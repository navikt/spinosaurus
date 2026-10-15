package no.nav.syfo.web.api

import java.math.BigDecimal
import java.time.LocalDate

data class RefusjonskravRequest(
    val inntektsmeldingIder: List<String>,
)

data class InntektsmeldingRefusjonskrav(
    val inntektsmeldingId: String,
    val refusjonskrav: Refusjonskrav?,
)

data class Refusjonskrav(
    val beloepPerMaaned: BigDecimal?,
    val endringer: List<Refusjonsendring>,
)

data class Refusjonsendring(
    val beloep: BigDecimal,
    val startdato: LocalDate,
)
