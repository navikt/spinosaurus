package no.nav.syfo.simba

import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.AarsakInnsending
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Arbeidsgiverperiode
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Avsender
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Inntekt
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Inntektsmelding
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Naturalytelse
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Permisjon
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.RedusertLoennIAgp
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Refusjon
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.RefusjonEndring
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Sykmeldt
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.api.AvsenderSystem
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.til
import no.nav.helsearbeidsgiver.utils.test.date.februar
import no.nav.helsearbeidsgiver.utils.test.date.januar
import no.nav.helsearbeidsgiver.utils.test.date.mars
import no.nav.helsearbeidsgiver.utils.test.wrapper.genererGyldig
import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import no.nav.helsearbeidsgiver.utils.wrapper.Orgnr
import java.time.ZoneOffset
import java.util.UUID

fun mockInntektsmelding(): Inntektsmelding =
    Inntektsmelding(
        id = UUID.randomUUID(),
        type =
            Inntektsmelding.Type.Forespurt(
                id = UUID.randomUUID(),
            ),
        vedtaksperiodeId = UUID.randomUUID(),
        sykmeldt =
            Sykmeldt(
                fnr = Fnr.genererGyldig(),
                navn = "Syk Sykesen",
            ),
        avsender =
            Avsender(
                orgnr = Orgnr.genererGyldig(),
                orgNavn = "Blåbærsyltetøy A/S",
                navn = "Hå Erresen",
                tlf = "22555555",
            ),
        sykmeldingsperioder =
            listOf(
                10.januar til 31.januar,
                10.februar til 28.februar,
            ),
        agp = mockAgp(),
        inntekt =
            Inntekt(
                beloep = 66_666.0,
                inntektsdato = 10.januar,
                endringAarsaker =
                    listOf(
                        Permisjon(
                            permisjoner =
                                listOf(
                                    6.januar til 6.januar,
                                    8.januar til 8.januar,
                                ),
                        ),
                    ),
            ),
        naturalytelser =
            listOf(
                Naturalytelse(
                    naturalytelse = Naturalytelse.Kode.BIL,
                    verdiBeloep = 123.0,
                    sluttdato = 1.februar,
                ),
                Naturalytelse(
                    naturalytelse = Naturalytelse.Kode.FRITRANSPORT,
                    verdiBeloep = 456.0,
                    sluttdato = 15.februar,
                ),
            ),
        refusjon =
            Refusjon(
                beloepPerMaaned = 22_222.0,
                endringer =
                    listOf(
                        RefusjonEndring(
                            beloep = 22_111.0,
                            startdato = 1.februar,
                        ),
                        RefusjonEndring(
                            beloep = 22_000.0,
                            startdato = 2.februar,
                        ),
                    ),
            ),
        aarsakInnsending = AarsakInnsending.Ny,
        mottatt = 1.mars.atStartOfDay().atOffset(ZoneOffset.ofHours(1)),
    )

fun mockAgp(): Arbeidsgiverperiode =
    Arbeidsgiverperiode(
        perioder =
            listOf(
                1.januar til 3.januar,
                5.januar til 5.januar,
                10.januar til 21.januar,
            ),
        egenmeldinger =
            listOf(
                1.januar til 3.januar,
                5.januar til 5.januar,
            ),
        redusertLoennIAgp =
            RedusertLoennIAgp(
                beloep = 55_555.0,
                begrunnelse = RedusertLoennIAgp.Begrunnelse.Permittering,
            ),
    )

fun mockAvsenderSystem(): AvsenderSystem =
    AvsenderSystem(
        orgnr = Orgnr.genererGyldig(),
        navn = "Hans Christians Hevn",
        versjon = "første og siste",
    )
