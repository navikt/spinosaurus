package no.nav.syfo.simba

import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Inntektsmelding

fun Inntektsmelding.skalSendesTilSpleis(): Boolean {
    val imType = this.type
    return when (imType) {
        is Inntektsmelding.Type.Selvbestemt, is Inntektsmelding.Type.Forespurt, is Inntektsmelding.Type.ForespurtEkstern -> true
        is Inntektsmelding.Type.Fisker, is Inntektsmelding.Type.UtenArbeidsforhold, is Inntektsmelding.Type.Behandlingsdager -> false
    }
}
