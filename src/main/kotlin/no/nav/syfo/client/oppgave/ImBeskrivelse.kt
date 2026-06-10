package no.nav.syfo.client.oppgave

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
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
            add("Utdrag av info, se vedlagt inntektsmelding (PDF) for full informasjon.")

            add("")
            if (inntektsmelding.inntektsdato != null) {
                add("Inntektsdato: ${inntektsmelding.inntektsdato.tilNorskFormat()}")
            } else if (inntektsmelding.førsteFraværsdag != null) {
                // førsteFraværsdag koden kan slettes når ingen utsatt oppgave er av typen Altinn2 inntektsmelding
                add("Bestemmende fraværsdag: ${inntektsmelding.førsteFraværsdag.tilNorskFormat()}")
            }
            inntektsmelding.inntektsdato?.let { add("Inntektsdato: ${it.tilNorskFormat()}") }
            add("Arbeidsgiverperiode: ${inntektsmelding.arbeidsgiverperioder.tilKortFormat().orDefault("Ingen")}")

            add("")
            inntektsmelding.beregnetInntekt?.let { add("Beregnet månedslønn: ${it.tilNorskFormat()} kr") }

            if (refusjon.beloepPrMnd != null) {
                add("Refusjon: ${refusjon.beloepPrMnd} kr/mnd")
                refusjon.opphoersdato?.let { add("Refusjon opphører: ${it.tilNorskFormat()}") }
            } else {
                add("Ingen refusjon - utbetaling til bruker")
            }

            inntektsmelding.endringerIRefusjon.forEach { endring ->
                val beloep = endring.beloep?.let { "$it kr" } ?: "Ukjent beløp"
                val dato = endring.endringsdato?.let { " fra ${it.tilNorskFormat()}" }.orEmpty()
                add("Endring i refusjon: $beloep$dato")
            }

            if (inntektsmelding.begrunnelseRedusert.isNotEmpty()) {
                add("Begrunnelse redusert AGP: ${inntektsmelding.begrunnelseRedusert}")
            }

            inntektsmelding.opphørAvNaturalYtelse.forEach { opphoer ->
                val ytelse = opphoer.naturalytelse?.name ?: "Naturalytelse"
                val beloep = opphoer.beloepPrMnd?.let { " ($it kr/mnd)" }.orEmpty()
                val dato = opphoer.fom?.let { " fra ${it.tilNorskFormat()}" }.orEmpty()
                add("Bortfall av naturalytelse: $ytelse$beloep$dato")
            }

            add("")
            add("Forespurt: ${if (inntektsmelding.forespurt) "Ja" else "Nei"}")
            if (inntektsmelding.nærRelasjon == true) add("Nær relasjon: Ja")
            if (inntektsmelding.harFlereArbeidsforhold) add("Flere arbeidsforhold: Ja")
        }

    return linjer.joinToString("\n")
}


private val inntektFormat =
    DecimalFormat(
        "#,##0.00",
        DecimalFormatSymbols().apply {
            groupingSeparator = ' '
            decimalSeparator = ','
        },
    )

fun BigDecimal.tilNorskFormat(): String =
    setScale(2, RoundingMode.HALF_UP)
        .let(inntektFormat::format)
