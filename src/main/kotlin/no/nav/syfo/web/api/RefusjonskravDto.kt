package no.nav.syfo.web.api

import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

data class RefusjonskravRequest(
    val inntektsmeldingIder: List<UUID>,
)

data class InntektsmeldingRefusjonskrav(
    val inntektsmeldingId: UUID,
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
