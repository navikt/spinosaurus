package no.nav.syfo.mapping

import no.nav.helsearbeidsgiver.utils.test.wrapper.genererGyldig
import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import no.nav.inntektsmeldingkontrakt.Arbeidsgivertype
import no.nav.syfo.FØRSTE_FEBRUAR
import no.nav.syfo.FØRSTE_JANUAR
import no.nav.syfo.domain.inntektsmelding.RapportertInntekt
import no.nav.syfo.dto.InntektsmeldingEntitet
import no.nav.syfo.grunnleggendeInntektsmelding
import no.nav.syfo.util.InntektsmeldingJsonMapper
import no.nav.syfo.util.customObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.UUID

class InntektsmeldingKontraktMapperKtTest {
    val om = customObjectMapper()

    @Test
    fun toInntektsmelding() {
        val dto =
            InntektsmeldingEntitet(
                uuid = UUID.randomUUID().toString(),
                aktorId = grunnleggendeInntektsmelding.aktorId.toString(),
                fnr = Fnr.genererGyldig(),
                data = om.writeValueAsString(grunnleggendeInntektsmelding),
            )
        val i = InntektsmeldingJsonMapper.fromJson(dto.data, dto.fnr)
        assertThat(i.journalpostId).isEqualTo("123")
        assertThat(i.sakId).isEqualTo("sakId")
        assertThat(i.arbeidsgiverOrgnummer).isEqualTo("1234")
        assertThat(i.fnr).isEqualTo(dto.fnr.verdi)
        assertThat(i.aktorId).isEqualTo("aktorId")
        assertThat(i.arbeidsgiverperioder.size).isEqualTo(1)
    }

    @Test
    fun `toInntektsmelding oppdaterer manglende fnr`() {
        val inntektsmeldingUtenFnr =
            grunnleggendeInntektsmelding.copy(
                fnr = "",
            )
        val dto =
            InntektsmeldingEntitet(
                uuid = UUID.randomUUID().toString(),
                aktorId = inntektsmeldingUtenFnr.aktorId.toString(),
                fnr = Fnr.genererGyldig(),
                data = om.writeValueAsString(inntektsmeldingUtenFnr),
            )
        val i = InntektsmeldingJsonMapper.fromJson(dto.data, dto.fnr)
        assertThat(i.journalpostId).isEqualTo("123")
        assertThat(i.sakId).isEqualTo("sakId")
        assertThat(i.arbeidsgiverOrgnummer).isEqualTo("1234")
        assertThat(i.fnr).isEqualTo(dto.fnr.verdi)
        assertThat(i.aktorId).isEqualTo("aktorId")
        assertThat(i.arbeidsgiverperioder.size).isEqualTo(1)
    }

    @Test
    fun skal_mappe_enkel_periode() {
        val mappedePerioder = mapArbeidsgiverperioder(grunnleggendeInntektsmelding)
        assertThat(mappedePerioder.size).isEqualTo(1)
        assertThat(mappedePerioder[0].fom).isEqualTo(FØRSTE_JANUAR)
        assertThat(mappedePerioder[0].tom).isEqualTo(FØRSTE_FEBRUAR)
    }

    @Test
    fun skal_finne_arbeidsgivertype_virksomhet() {
        assertThat(mapArbeidsgivertype(grunnleggendeInntektsmelding)).isEqualTo(Arbeidsgivertype.VIRKSOMHET)
    }

    @Test
    fun skal_finne_arbeidsgivertype_privat() {
        assertThat(
            mapArbeidsgivertype(
                grunnleggendeInntektsmelding.copy(arbeidsgiverOrgnummer = null, arbeidsgiverPrivatFnr = "00"),
            ),
        ).isEqualTo(Arbeidsgivertype.PRIVAT)
    }

    @Test
    fun `RapportertInntekt med endringAarsakerData blir deserialisert riktig `() {
        val rapportertInntektJson =
            """
            {
                "bekreftet": true,
                "endringAarsak": null,
                "beregnetInntekt": 49000.0,
                "manueltKorrigert": true,
                "endringAarsakerData": [{
                    "aarsak": "NyStillingsprosent",
                    "bleKjent": null,
                    "perioder": null,
                    "gjelderFra": "2024-11-01"
                  }]
              }
            """.trimIndent()
        val rapportertInntekt = om.readValue(rapportertInntektJson, RapportertInntekt::class.java)
        assertThat(rapportertInntekt.endringAarsakerData).isNotEmpty()
        assertThat(rapportertInntekt.manueltKorrigert).isTrue()
        assertThat(rapportertInntekt.endringAarsakerData.first().aarsak).isEqualTo("NyStillingsprosent")
        assertThat(rapportertInntekt.endringAarsakerData.first().gjelderFra).isEqualTo("2024-11-01")
        assertThat(rapportertInntekt.endringAarsakerData.first().bleKjent).isNull()
        assertThat(rapportertInntekt.endringAarsakerData.first().perioder).isNull()
    }
}
