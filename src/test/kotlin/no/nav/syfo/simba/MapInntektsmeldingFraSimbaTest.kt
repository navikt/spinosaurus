package no.nav.syfo.simba

import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Bonus
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Inntekt
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Inntektsmelding
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Naturalytelse
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.RedusertLoennIAgp
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Refusjon
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.RefusjonEndring
import no.nav.helsearbeidsgiver.utils.test.date.desember
import no.nav.helsearbeidsgiver.utils.test.date.mai
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID
import no.nav.syfo.simba.Avsender as MuligAvsender

class MapInntektsmeldingFraSimbaTest {
    @Test
    fun mapInntektsmeldingMedNaturalytelser() {
        val naturalytelser = Naturalytelse.Kode.entries.map { Naturalytelse(it, 1.0, LocalDate.now()) }
        val antallNaturalytelser = naturalytelser.count()
        val imd =
            mockInntektsmelding().copy(
                naturalytelser = naturalytelser,
            )
        val mapped =
            mapInntektsmelding(
                arkivreferanse = "im1323",
                aktorId = "sdfds",
                journalpostId = "134",
                im = imd,
            )
        assertEquals(antallNaturalytelser, mapped.opphørAvNaturalYtelse.size)
        val naturalytelse = mapped.opphørAvNaturalYtelse[0]
        assertEquals(no.nav.syfo.domain.inntektsmelding.Naturalytelse.AKSJERGRUNNFONDSBEVISTILUNDERKURS, naturalytelse.naturalytelse)
    }

    @Test
    fun mapRefusjon() {
        val refusjonEndringer = listOf(RefusjonEndring(123.0, 1.desember(2025)))
        val refusjon = Refusjon(10.0, refusjonEndringer)
        val mapped =
            mapInntektsmelding(
                arkivreferanse = "im1323",
                aktorId = "sdfds",
                journalpostId = "134",
                im = mockInntektsmelding().copy(refusjon = refusjon),
            )
        assertNull(mapped.refusjon.opphoersdato)
        assertEquals(mapped.endringerIRefusjon.size, 1)
    }

    @Test
    fun mapBegrunnelseRedusert() {
        RedusertLoennIAgp.Begrunnelse.entries.forEach { begrunnelse ->
            val im =
                mockInntektsmelding().let {
                    it.copy(
                        agp =
                            it.agp?.copy(
                                redusertLoennIAgp =
                                    RedusertLoennIAgp(
                                        beloep = 1.0,
                                        begrunnelse = begrunnelse,
                                    ),
                            ),
                    )
                }

            val mapped = mapInntektsmelding("im123", "abc", "345", im)

            assertEquals(begrunnelse.name, mapped.begrunnelseRedusert, "Feil ved mapping: $begrunnelse")
            assertEquals(1.0.toBigDecimal(), mapped.bruttoUtbetalt, "Feil ved mapping: $begrunnelse")
        }
    }

    @Test
    fun mapIngenBegrunnelseRedusert() {
        val im =
            mockInntektsmelding().let {
                it.copy(
                    agp =
                        it.agp?.copy(
                            redusertLoennIAgp = null,
                        ),
                )
            }
        val mapped = mapInntektsmelding("im1", "2", "3", im)
        assertEquals("", mapped.begrunnelseRedusert)
        assertNull(mapped.bruttoUtbetalt)
    }

    @Test
    fun mapInntektEndringAarsak() {
        val im =
            mockInntektsmelding().copy(
                inntekt =
                    Inntekt(
                        beloep = 60_000.0,
                        inntektsdato = 3.mai,
                        endringAarsaker = listOf(Bonus),
                    ),
            )
        val mapped = mapInntektsmelding("im1", "2", "3", im)
        val endringAarsak = mapped.rapportertInntekt?.endringAarsakerData?.get(0)!!
        assertEquals("Bonus", endringAarsak.aarsak)
        assertNull(endringAarsak.perioder)
        assertNull(endringAarsak.gjelderFra)
        assertNull(endringAarsak.bleKjent)
    }

    @Test
    fun mapInnsendtTidspunktFraSimba() {
        val localDateTime = LocalDateTime.of(2023, 2, 11, 14, 0)
        val innsendt = OffsetDateTime.of(localDateTime, ZoneOffset.of("+1"))
        val im = mapInntektsmelding("im1", "2", "3", mockInntektsmelding().copy(mottatt = innsendt))
        assertEquals(localDateTime, im.innsendingstidspunkt)
    }

    @Test
    fun mapVedtaksperiodeID() {
        val im = mapInntektsmelding("im1", "2", "3", mockInntektsmelding().copy(vedtaksperiodeId = null))
        assertNull(im.vedtaksperiodeId)
        val vedtaksperiodeId = UUID.randomUUID()
        val im2 = mapInntektsmelding("im1", "2", "3", mockInntektsmelding().copy(vedtaksperiodeId = vedtaksperiodeId))
        assertEquals(vedtaksperiodeId, im2.vedtaksperiodeId)
    }

    @Test
    fun mapAvsenderForSelvbestemtOgVanlig() {
        val selvbestemtIm =
            mockInntektsmelding().copy(
                type = Inntektsmelding.Type.Selvbestemt(UUID.randomUUID()),
            )
        val selvbestemtMapped = mapInntektsmelding("im1", "2", "3", selvbestemtIm)
        assertEquals(MuligAvsender.NAV_NO_SELVBESTEMT, selvbestemtMapped.avsenderSystem.navn)
        assertEquals("1.0", selvbestemtMapped.avsenderSystem.versjon)

        val mapped = mapInntektsmelding("im1", "2", "3", mockInntektsmelding())
        assertEquals(MuligAvsender.NAV_NO, mapped.avsenderSystem.navn)
        assertEquals("1.0", mapped.avsenderSystem.versjon)
    }
}
