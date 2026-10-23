package no.nav.syfo.mapping

import no.nav.inntektsmeldingkontrakt.Arbeidsgivertype
import no.nav.inntektsmeldingkontrakt.ArsakTilInnsending
import no.nav.inntektsmeldingkontrakt.AvsenderSystem
import no.nav.inntektsmeldingkontrakt.EndringIRefusjon
import no.nav.inntektsmeldingkontrakt.Format
import no.nav.inntektsmeldingkontrakt.GjenopptakelseNaturalytelse
import no.nav.inntektsmeldingkontrakt.InntektEndringAarsak
import no.nav.inntektsmeldingkontrakt.MottaksKanal
import no.nav.inntektsmeldingkontrakt.Naturalytelse
import no.nav.inntektsmeldingkontrakt.OpphoerAvNaturalytelse
import no.nav.inntektsmeldingkontrakt.Periode
import no.nav.inntektsmeldingkontrakt.Refusjon
import no.nav.inntektsmeldingkontrakt.Status
import no.nav.syfo.domain.inntektsmelding.Gyldighetsstatus
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding
import no.nav.syfo.domain.inntektsmelding.SpinnInntektEndringAarsak
import org.slf4j.LoggerFactory
import no.nav.syfo.domain.inntektsmelding.MottaksKanal as Kanal

private val logger = LoggerFactory.getLogger("InntektsmeldingMapper")

fun mapInntektsmeldingKontrakt(
    inntektsmelding: Inntektsmelding,
    arbeidstakerAktørId: String,
    gyldighetsstatus: Gyldighetsstatus,
    arkivreferanse: String,
    uuid: String,
): no.nav.inntektsmeldingkontrakt.Inntektsmelding =
    no.nav.inntektsmeldingkontrakt.Inntektsmelding(
        inntektsmeldingId = uuid,
        vedtaksperiodeId = inntektsmelding.vedtaksperiodeId,
        arbeidstakerFnr = inntektsmelding.fnr,
        arbeidstakerAktorId = arbeidstakerAktørId,
        virksomhetsnummer = inntektsmelding.arbeidsgiverOrgnummer,
        arbeidsgiverFnr = inntektsmelding.arbeidsgiverPrivatFnr,
        arbeidsgiverAktorId = inntektsmelding.arbeidsgiverPrivatAktørId,
        arbeidsgivertype = mapArbeidsgivertype(inntektsmelding),
        arbeidsforholdId = inntektsmelding.arbeidsforholdId,
        arbeidsgiverperioder = mapArbeidsgiverperioder(inntektsmelding),
        beregnetInntekt = inntektsmelding.beregnetInntekt,
        inntektsdato = inntektsmelding.inntektsdato,
        refusjon = mapRefusjon(inntektsmelding),
        endringIRefusjoner = mapEndringIRefusjon(inntektsmelding),
        opphoerAvNaturalytelser = mapOpphørAvNaturalytelser(inntektsmelding),
        gjenopptakelseNaturalytelser = mapGjenopptakelseAvNaturalytelser(inntektsmelding),
        status = mapStatus(gyldighetsstatus),
        arkivreferanse = arkivreferanse,
        ferieperioder = mapFerieperioder(inntektsmelding),
        foersteFravaersdag = inntektsmelding.førsteFraværsdag,
        mottattDato = inntektsmelding.mottattDato,
        begrunnelseForReduksjonEllerIkkeUtbetalt = inntektsmelding.begrunnelseRedusert,
        bruttoUtbetalt = inntektsmelding.bruttoUtbetalt,
        innsenderFulltNavn = inntektsmelding.kontaktinformasjon.navn.orEmpty(),
        innsenderTelefon = inntektsmelding.kontaktinformasjon.telefon.orEmpty(),
        naerRelasjon = inntektsmelding.nærRelasjon,
        avsenderSystem = mapAvsenderSystem(inntektsmelding.avsenderSystem),
        // TODO: inntektEndringAarsak kan fjernes når flex og spleis bare leser inntektEndringAarsaker
        inntektEndringAarsak =
            inntektsmelding.rapportertInntekt
                ?.endringAarsakerData
                ?.firstOrNull()
                ?.tilInntektEndringAarsak(),
        inntektEndringAarsaker =
            inntektsmelding.rapportertInntekt
                ?.endringAarsakerData
                .orEmpty()
                .map { it.tilInntektEndringAarsak() },
        arsakTilInnsending = konverterArsakTilInnsending(inntektsmelding.arsakTilInnsending),
        mottaksKanal = inntektsmelding.mottaksKanal.konverterMottakskanal(),
        format = inntektsmelding.mottaksKanal.utledFormat(),
        forespurt = inntektsmelding.forespurt,
        harFlereArbeidsforhold = inntektsmelding.harFlereArbeidsforhold,
    )

private fun konverterArsakTilInnsending(arsakTilInnsending: String): ArsakTilInnsending =
    try {
        ArsakTilInnsending.valueOf(arsakTilInnsending)
    } catch (e: IllegalArgumentException) {
        logger.error("Ugyldig verdi for årsakTilInnsending: $arsakTilInnsending, returnerer ${ArsakTilInnsending.Ny}")
        ArsakTilInnsending.Ny
    }

private fun Kanal.konverterMottakskanal(): MottaksKanal =
    when (this) {
        Kanal.NAV_NO -> MottaksKanal.NAV_NO
        Kanal.HR_SYSTEM_API -> MottaksKanal.HR_SYSTEM_API
        Kanal.ALTINN -> MottaksKanal.ALTINN
    }

private fun Kanal.utledFormat(): Format =
    when (this) {
        Kanal.ALTINN -> Format.Inntektsmelding
        else -> Format.Arbeidsgiveropplysninger
    }

private fun SpinnInntektEndringAarsak.tilInntektEndringAarsak(): InntektEndringAarsak =
    InntektEndringAarsak(
        aarsak = aarsak,
        perioder = perioder?.map { Periode(it.fom, it.tom) },
        gjelderFra = gjelderFra,
        bleKjent = bleKjent,
    )

private fun mapFerieperioder(inntektsmelding: Inntektsmelding): List<Periode> = inntektsmelding.feriePerioder.map { p -> Periode(p.fom, p.tom) }

private fun mapStatus(status: Gyldighetsstatus): Status {
    if (status == Gyldighetsstatus.GYLDIG) {
        return Status.GYLDIG
    }
    return Status.MANGELFULL
}

fun mapArbeidsgiverperioder(inntektsmelding: Inntektsmelding): List<Periode> = inntektsmelding.arbeidsgiverperioder.map { p -> Periode(p.fom, p.tom) }

fun mapArbeidsgivertype(inntektsmelding: Inntektsmelding): Arbeidsgivertype {
    if (inntektsmelding.arbeidsgiverOrgnummer.isNullOrEmpty()) {
        return Arbeidsgivertype.PRIVAT
    }
    return Arbeidsgivertype.VIRKSOMHET
}

private fun mapGjenopptakelseAvNaturalytelser(inntektsmelding: Inntektsmelding): List<GjenopptakelseNaturalytelse> =
    inntektsmelding.gjenopptakelserNaturalYtelse.map { gjenopptakelse ->
        GjenopptakelseNaturalytelse(
            mapNaturalytelseType(gjenopptakelse.naturalytelse),
            gjenopptakelse.fom,
            gjenopptakelse.beloepPrMnd,
        )
    }

private fun mapOpphørAvNaturalytelser(inntektsmelding: Inntektsmelding): List<OpphoerAvNaturalytelse> =
    inntektsmelding.opphørAvNaturalYtelse.map { opphør ->
        OpphoerAvNaturalytelse(
            mapNaturalytelseType(opphør.naturalytelse),
            opphør.fom,
            opphør.beloepPrMnd,
        )
    }

private fun mapEndringIRefusjon(inntektsmelding: Inntektsmelding): List<EndringIRefusjon> =
    inntektsmelding.endringerIRefusjon.map { endring -> EndringIRefusjon(endring.endringsdato, endring.beloep) }

private fun mapRefusjon(inntektsmelding: Inntektsmelding): Refusjon = Refusjon(inntektsmelding.refusjon.beloepPrMnd, inntektsmelding.refusjon.opphoersdato)

private fun mapNaturalytelseType(naturalytelseType: no.nav.syfo.domain.inntektsmelding.Naturalytelse?): Naturalytelse =
    naturalytelseType?.let { naturalytelse ->
        if (Naturalytelse.entries.map { it.name }.contains(
                naturalytelse.name,
            )
        ) {
            Naturalytelse.valueOf(naturalytelse.name)
        } else {
            Naturalytelse.ANNET
        }
    }
        ?: Naturalytelse.ANNET

private fun mapAvsenderSystem(avsenderSystem: no.nav.syfo.domain.inntektsmelding.AvsenderSystem): AvsenderSystem =
    AvsenderSystem(
        navn = avsenderSystem.navn,
        versjon = avsenderSystem.versjon,
    )
