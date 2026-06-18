package no.nav.syfo.client.oppgave

import no.nav.syfo.domain.inntektsmelding.Refusjon
import no.nav.syfo.repository.buildIM
import no.nav.syfo.utsattoppgave.BehandlingsKategori
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ImBeskrivelseTest {
    @Test
    fun `beskrivelse med refusjon`() {
        val inntektsmelding = buildIM()

        val forventet =
            """
            Refusjon: Ja | Kategori: Utbetaling til bruker
            Inntektsmelding sykepenger
            Utdrag av info, se vedlagt inntektsmelding (PDF) for full informasjon.

            Bestemmende fraværsdag: 10.02.2010
            Arbeidsgiverperiode: 01.11.2011 - [...] - 04.04.2014

            Beregnet månedslønn: 999 999 999 999,00 kr
            Refusjon: 333 333 333 333,00 kr/mnd
            Refusjon opphører: 20.02.2020
            Endring i refusjon: 555 555 555 555,00 kr fra 05.05.2015
            Endring i refusjon: 666 666 666 666,00 kr fra 06.06.2016
            Begrunnelse redusert AGP: Grunn til reduksjon
            Bortfall av naturalytelse: BIL (555 555 555 555,00 kr/mnd) fra 05.05.2015
            Bortfall av naturalytelse: TILSKUDDBARNEHAGEPLASS (666 666 666 666,00 kr/mnd) fra 06.06.2016

            Forespurt: Nei
            """.trimIndent()

        assertEquals(forventet, lagInntektsmeldingOppgaveBeskrivelse(inntektsmelding, BehandlingsKategori.REFUSJON_MED_DATO))
    }

    @Test
    fun `beskrivelse med refusjon 0 kr`() {
        val inntektsmelding = buildIM().copy(refusjon = Refusjon(beloepPrMnd = BigDecimal.ZERO))
        val beskrivelse = lagInntektsmeldingOppgaveBeskrivelse(inntektsmelding, BehandlingsKategori.REFUSJON_MED_DATO)
        assert(beskrivelse.contains("Refusjon: Ja (0 kr) | "))
    }

    @Test
    fun `beskrivelse uten refusjon`() {
        val inntektsmelding =
            buildIM().copy(
                arbeidsgiverperioder = emptyList(),
                refusjon = Refusjon(),
                endringerIRefusjon = emptyList(),
                opphørAvNaturalYtelse = emptyList(),
                begrunnelseRedusert = "",
                forespurt = true,
                nærRelasjon = true,
                harFlereArbeidsforhold = true,
            )

        val forventet =
            """
            Refusjon: Nei | Kategori: Bestrider sykmelding
            Inntektsmelding sykepenger
            Utdrag av info, se vedlagt inntektsmelding (PDF) for full informasjon.

            Bestemmende fraværsdag: 10.02.2010
            Arbeidsgiverperiode: Ingen

            Beregnet månedslønn: 999 999 999 999,00 kr
            Ingen refusjon - utbetaling til bruker

            Forespurt: Ja
            Nær relasjon: Ja
            Flere arbeidsforhold: Ja
            """.trimIndent()

        assertEquals(forventet, lagInntektsmeldingOppgaveBeskrivelse(inntektsmelding, BehandlingsKategori.BESTRIDER_SYKEMELDING))
    }
}
