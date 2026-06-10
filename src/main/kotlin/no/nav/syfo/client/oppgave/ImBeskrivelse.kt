package no.nav.syfo.client.oppgave

import no.nav.helsearbeidsgiver.utils.pipe.orDefault
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding
import no.nav.syfo.domain.tilKortFormat
import no.nav.syfo.domain.tilNorskFormat
import no.nav.syfo.utsattoppgave.BehandlingsKategori

fun lagInntektsmeldingOppgaveBeskrivelse(
    inntektsmelding: Inntektsmelding,
    behandlingsKategori: BehandlingsKategori,
): String {
    val linjer =
        buildList {
            val refusjon = inntektsmelding.refusjon
            val erRefusjon = refusjon.beloepPrMnd != null
            val kategori = behandlingsKategori.oppgaveBeskrivelse ?: behandlingsKategori.name
            add("Refusjon: ${if (erRefusjon) "Ja" else "Nei"} | Kategori: $kategori")
            add("Inntektsmelding sykepenger")
            add("Utdrag av info, se vedlagt inntektsmeldingen (PDF) for full informasjon.")

            add("")
            inntektsmelding.førsteFraværsdag?.let { add("Bestemmende fraværsdag: ${it.tilNorskFormat()}") }
            add("Arbeidsgiverperiode: ${inntektsmelding.arbeidsgiverperioder.tilKortFormat().orDefault("Ingen")}")

            add("")
            inntektsmelding.beregnetInntekt?.let { add("Beregnet månedslønn: $it kr") }

            if (refusjon.beloepPrMnd != null) {
                add("Refusjon: ${refusjon.beloepPrMnd} kr/mnd")
                refusjon.opphoersdato?.let { add("Refusjon opphører: ${it.tilNorskFormat()}") }
            } else {
                add("Ingen refusjon - utbetaling til bruker")
            }

            inntektsmelding.endringerIRefusjon.forEach { endring ->
                val beloep = endring.beloep?.let { "$it kr" } ?: "ukjent beløp"
                val dato = endring.endringsdato?.let { " fra ${it.tilNorskFormat()}" } ?: ""
                add("Endring i refusjon: $beloep$dato")
            }

            if (inntektsmelding.begrunnelseRedusert.isNotEmpty()) {
                add("Begrunnelse redusert AGP: ${inntektsmelding.begrunnelseRedusert}")
            }

            inntektsmelding.opphørAvNaturalYtelse.forEach { opphoer ->
                val ytelse = opphoer.naturalytelse?.name ?: "Naturalytelse"
                val beloep = opphoer.beloepPrMnd?.let { " ($it kr/mnd)" } ?: ""
                val dato = opphoer.fom?.let { " fra ${it.tilNorskFormat()}" } ?: ""
                add("Bortfall av naturalytelse: $ytelse$beloep$dato")
            }

            add("")
            add("Forespurt: ${if (inntektsmelding.forespurt) "Ja" else "Nei"}")
            if (inntektsmelding.nærRelasjon == true) add("Nær relasjon: Ja")
            if (inntektsmelding.harFlereArbeidsforhold) add("Flere arbeidsforhold: Ja")
        }

    return linjer.joinToString("\n")
}
