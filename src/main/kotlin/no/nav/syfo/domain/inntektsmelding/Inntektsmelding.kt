package no.nav.syfo.domain.inntektsmelding

import no.nav.syfo.domain.JournalStatus
import no.nav.syfo.domain.Periode
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class Inntektsmelding(
    val id: String = "",
    var fnr: String,
    val arbeidsgiverOrgnummer: String? = null,
    val arbeidsgiverPrivatFnr: String? = null,
    val arbeidsgiverPrivatAktørId: String? = null,
    val arbeidsforholdId: String? = null,
    val journalpostId: String,
    val arsakTilInnsending: String,
    var journalStatus: JournalStatus,
    val arbeidsgiverperioder: List<Periode> = emptyList(),
    val beregnetInntekt: BigDecimal? = null,
    val inntektsdato: LocalDate? = null,
    val refusjon: Refusjon = Refusjon(),
    val endringerIRefusjon: List<EndringIRefusjon> = emptyList(),
    val opphørAvNaturalYtelse: List<OpphoerAvNaturalytelse> = emptyList(),
    val gjenopptakelserNaturalYtelse: List<GjenopptakelseNaturalytelse> = emptyList(),
    val gyldighetsStatus: Gyldighetsstatus = Gyldighetsstatus.GYLDIG,
    val arkivRefereranse: String,
    val feriePerioder: List<Periode> = emptyList(),
    val førsteFraværsdag: LocalDate?,
    val mottattDato: LocalDateTime,
    var sakId: String? = null,
    var aktorId: String? = null,
    val begrunnelseRedusert: String = "",
    val avsenderSystem: AvsenderSystem = AvsenderSystem(),
    val nærRelasjon: Boolean? = null,
    val kontaktinformasjon: Kontaktinformasjon = Kontaktinformasjon(),
    val innsendingstidspunkt: LocalDateTime? = null,
    val bruttoUtbetalt: BigDecimal? = null,
    val årsakEndring: String? = null,
    val rapportertInntekt: RapportertInntekt? = null,
    val vedtaksperiodeId: UUID? = null,
    val mottaksKanal: MottaksKanal = utledMottakskanal(avsenderSystem.navn),
    val forespurt: Boolean = false,
) {
    fun isDuplicate(inntektsmelding: Inntektsmelding): Boolean =
        this.equals(
            inntektsmelding.copy(
                id = this.id,
                fnr = this.fnr,
                mottattDato = this.mottattDato,
                journalpostId = this.journalpostId,
                journalStatus = this.journalStatus,
                arkivRefereranse = this.arkivRefereranse,
                aktorId = this.aktorId,
                sakId = this.sakId,
                innsendingstidspunkt = this.innsendingstidspunkt,
                avsenderSystem = this.avsenderSystem,
                mottaksKanal = this.mottaksKanal,
            ),
        )

    fun isDuplicateExclusiveArsakInnsending(inntektsmelding: Inntektsmelding): Boolean =
        this.equals(
            inntektsmelding.copy(
                id = this.id,
                fnr = this.fnr,
                mottattDato = this.mottattDato,
                journalpostId = this.journalpostId,
                journalStatus = this.journalStatus,
                arkivRefereranse = this.arkivRefereranse,
                aktorId = this.aktorId,
                sakId = this.sakId,
                innsendingstidspunkt = this.innsendingstidspunkt,
                avsenderSystem = this.avsenderSystem,
                mottaksKanal = this.mottaksKanal,
                arsakTilInnsending = this.arsakTilInnsending,
            ),
        )
}

// For å støtte gamle verdier som mangler mottakskanal
private fun utledMottakskanal(ansenderSystemNavn: String?): MottaksKanal =
    when (ansenderSystemNavn) {
        "NAV_NO",
        "NAV_NO_SELVBESTEMT",
        -> MottaksKanal.NAV_NO
        else -> MottaksKanal.ALTINN
    }
