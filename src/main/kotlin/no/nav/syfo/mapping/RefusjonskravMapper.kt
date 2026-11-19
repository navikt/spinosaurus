package no.nav.syfo.mapping

import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.RefusjonEndring
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.syfo.domain.inntektsmelding.EndringIRefusjon
import no.nav.syfo.domain.inntektsmelding.Refusjon
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Refusjon as RefusjonSimba

fun tilRefusjonskrav(
    refusjon: Refusjon,
    endringerIRefusjon: List<EndringIRefusjon>,
    inntektsmeldingId: String,
): RefusjonSimba? {
    val beloepPerMaaned = refusjon.beloepPrMnd?.toDouble() ?: return null

    val refusjonsendringer =
        endringerIRefusjon.mapNotNull { endring ->
            val dato = endring.endringsdato
            val beloep = endring.beloep
            if (dato != null && beloep != null) RefusjonEndring(beloep.toDouble(), dato) else null
        }

    val refusjonsopphoer = refusjon.opphoersdato?.let { RefusjonEndring(0.0, it) }

    return RefusjonSimba(
        beloepPerMaaned = beloepPerMaaned,
        endringer =
            refusjonsendringer
                .adderHvisIkkeKollisjon(refusjonsopphoer) {
                    "refusjonskrav".logger().warn(
                        "Opphørsdato ${refusjonsopphoer?.startdato} kolliderer med refusjonsendring for inntektsmelding $inntektsmeldingId. " +
                            "Opphørsdato ignoreres og refusjonsendring beholdes.",
                    )
                }.sortedBy { it.startdato },
    )
}

private fun List<RefusjonEndring>.adderHvisIkkeKollisjon(
    refusjonsendring: RefusjonEndring?,
    hvisKollisjon: () -> Unit,
): List<RefusjonEndring> =
    when {
        refusjonsendring == null -> this
        any { it.startdato == refusjonsendring.startdato } -> this.also { hvisKollisjon() }
        else -> this + refusjonsendring
    }
