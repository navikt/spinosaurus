package no.nav.syfo.domain.inntektsmelding

import no.nav.syfo.domain.Periode
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Inntektsmelding(
    val fnr: String,
    val arbeidsgiverOrgnummer: String? = null,
    val arbeidsgiverPrivatFnr: String? = null,
    val arbeidsgiverPrivatAktørId: String? = null,
    val arsakTilInnsending: String,
    val arbeidsgiverperioder: List<Periode> = emptyList(),
    val beregnetInntekt: BigDecimal? = null,
    val inntektsdato: LocalDate? = null,
    val refusjon: Refusjon = Refusjon(),
    val endringerIRefusjon: List<EndringIRefusjon> = emptyList(),
    val opphørAvNaturalYtelse: List<OpphoerAvNaturalytelse> = emptyList(),
    val gjenopptakelserNaturalYtelse: List<GjenopptakelseNaturalytelse> = emptyList(),
    val arkivRefereranse: String,
    val feriePerioder: List<Periode> = emptyList(),
    val førsteFraværsdag: LocalDate?,
    val mottattDato: LocalDateTime,
    var aktorId: String? = null,
    val begrunnelseRedusert: String = "",
    val avsenderSystem: AvsenderSystem = AvsenderSystem(),
    val nærRelasjon: Boolean? = null,
    val kontaktinformasjon: Kontaktinformasjon = Kontaktinformasjon(),
    val bruttoUtbetalt: BigDecimal? = null,
    val rapportertInntekt: RapportertInntekt? = null,
    val vedtaksperiodeId: UUID? = null,
    val mottaksKanal: MottaksKanal = utledMottakskanal(avsenderSystem.navn),
    val forespurt: Boolean = false,
    val harFlereArbeidsforhold: Boolean = false,
)

// For å støtte gamle verdier som mangler mottakskanal
private fun utledMottakskanal(ansenderSystemNavn: String?): MottaksKanal =
    when (ansenderSystemNavn) {
        "NAV_NO",
        "NAV_NO_SELVBESTEMT",
        -> MottaksKanal.NAV_NO

        else -> MottaksKanal.ALTINN
    }
