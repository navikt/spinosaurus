package no.nav.syfo.mapping

import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding
import no.nav.syfo.web.api.Refusjonsendring
import no.nav.syfo.web.api.Refusjonskrav

fun Inntektsmelding.tilRefusjonskrav(inntektsmeldingId: String): Refusjonskrav {
    val refusjonsendringer =
        endringerIRefusjon.mapNotNull { endring ->
            val dato = endring.endringsdato
            val beloep = endring.beloep
            if (dato != null && beloep != null) Refusjonsendring(beloep.toDouble(), dato) else null
        }

    val refusjonsopphoer = refusjon.opphoersdato?.let { Refusjonsendring(0.0, it) }

    return Refusjonskrav(
        beloepPerMaaned = refusjon.beloepPrMnd?.toDouble(),
        endringer =
            refusjonsendringer
                .adderHvisIkkeKollisjon(refusjonsopphoer) {
                    logger().warn(
                        "Opphørsdato ${refusjonsopphoer?.startdato} kolliderer med refusjonsendring for inntektsmelding $inntektsmeldingId. " +
                            "Opphørsdato ignoreres og refusjonsendring beholdes.",
                    )
                }.sortedBy { it.startdato },
    )
}

private fun List<Refusjonsendring>.adderHvisIkkeKollisjon(
    refusjonsendring: Refusjonsendring?,
    hvisKollisjon: () -> Unit,
): List<Refusjonsendring> =
    when {
        refusjonsendring == null -> this
        any { it.startdato == refusjonsendring.startdato } -> this.also { hvisKollisjon() }
        else -> this + refusjonsendring
    }
