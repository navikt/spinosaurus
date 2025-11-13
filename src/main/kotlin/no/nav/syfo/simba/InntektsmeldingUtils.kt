package no.nav.syfo.simba

import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Inntektsmelding

fun Inntektsmelding.skalSendesTilSpleis(): Boolean {
    val imType = this.type
    return when (imType) {
        is Inntektsmelding.Type.Selvbestemt -> true
        is Inntektsmelding.Type.Fisker, is Inntektsmelding.Type.UtenArbeidsforhold, is Inntektsmelding.Type.Behandlingsdager -> false
        // Skal ikke sende ikke-forespurt AGP til Spleis
        is Inntektsmelding.Type.Forespurt -> imType.erAgpForespurt || agp == null
        is Inntektsmelding.Type.ForespurtEkstern -> imType.erAgpForespurt || agp == null
    }
}
