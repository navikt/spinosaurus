package no.nav.syfo.util

import no.nav.syfo.domain.inntektsmelding.Gyldighetsstatus
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding

fun validerInntektsmelding(inntektsmelding: Inntektsmelding): Gyldighetsstatus =
    if (erArbeidsforholdGyldig(inntektsmelding)) {
        Gyldighetsstatus.GYLDIG
    } else {
        Gyldighetsstatus.MANGELFULL
    }

private fun erArbeidsforholdGyldig(inntektsmelding: Inntektsmelding): Boolean =
    inntektsmelding.arbeidsgiverOrgnummer.isNullOrEmpty() xor
        inntektsmelding.arbeidsgiverPrivatFnr.isNullOrEmpty()
