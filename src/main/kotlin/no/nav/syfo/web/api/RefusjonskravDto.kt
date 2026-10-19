package no.nav.syfo.web.api

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
    val beloepPerMaaned: Double,
    val endringer: List<Refusjonsendring>,
)

data class Refusjonsendring(
    val beloep: Double,
    val startdato: LocalDate,
)
