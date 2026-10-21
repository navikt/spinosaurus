package no.nav.syfo.web.api

import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Refusjon
import java.util.UUID

data class RefusjonskravRequest(
    val inntektsmeldingIder: List<UUID>,
)

data class InntektsmeldingRefusjonskrav(
    val inntektsmeldingId: UUID,
    val refusjonskrav: Refusjon?,
)
