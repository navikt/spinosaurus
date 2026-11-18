package no.nav.syfo.service

import no.nav.inntektsmelding.kontrakt.serde.JacksonJsonConfig
import no.nav.syfo.repository.buildIM
import no.nav.syfo.util.InntektsmeldingJsonMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class InntektsmeldingServiceTest {
    @Test
    fun `Skal gi forventet JSON format`() {
        val im = buildIM()
        val json = InntektsmeldingJsonMapper.toJson(im)
        val mapper = JacksonJsonConfig.opprettObjectMapper()
        val node = mapper.readTree(json)
        assertThat(node.get("id").asText()).isEqualTo("id-abc")
        assertThat(node.get("fnr").asText()).isEqualTo("fnr-123") // Skal ikke lagre fødselsnummer
        assertThat(node.get("aktorId").asText()).isEqualTo("aktør-123")
        assertThat(node.get("refusjon").get("beloepPrMnd").asLong()).isEqualTo(333333333333)
        assertThat(node.get("refusjon").get("opphoersdato").toString()).isEqualTo("\"2020-02-20\"")
        assertThat(node.get("begrunnelseRedusert").asText()).isEqualTo("Grunn til reduksjon")
        assertThat(node.get("sakId").asText()).isEqualTo("sak-123")
        assertThat(node.get("mottattDato").toString()).isEqualTo("\"2010-05-04T03:02:01\"")
        assertThat(node.get("arkivRefereranse").asText()).isEqualTo("ar-123")
        assertThat(node.get("førsteFraværsdag").toString()).isEqualTo("\"2010-02-10\"")
        assertThat(node.get("arsakTilInnsending").asText()).isEqualTo("Ingen årsak")
        assertThat(node.get("journalpostId").asText()).isEqualTo("jp-123")
        assertThat(node.get("arbeidsforholdId").asText()).isEqualTo("arb-123")
        assertThat(node.get("arbeidsgiverPrivatFnr").asText()).isEqualTo("arb-priv-123")
        assertThat(node.get("arbeidsgiverOrgnummer").asText()).isEqualTo("arb-org-123")
        assertThat(node.get("arbeidsgiverPrivatAktørId").asText()).isEqualTo("arb-priv-aktør-123")
        assertThat(node.get("beregnetInntekt").asLong()).isEqualTo(999999999999)
        assertThat(node.get("gyldighetsStatus").asText()).isEqualTo("GYLDIG")
        assertThat(node.get("journalStatus").asText()).isEqualTo("MOTTATT")
        assertThat(node.get("arbeidsgiverperioder").size()).isEqualTo(2)
        assertThat(node.get("arbeidsgiverperioder")[0].get("fom").toString()).isEqualTo("\"2011-11-01\"")
        assertThat(node.get("arbeidsgiverperioder")[0].get("tom").toString()).isEqualTo("\"2012-12-02\"")
        assertThat(node.get("arbeidsgiverperioder")[1].get("fom").toString()).isEqualTo("\"2013-03-03\"")
        assertThat(node.get("arbeidsgiverperioder")[1].get("tom").toString()).isEqualTo("\"2014-04-04\"")
        assertThat(node.get("endringerIRefusjon").size()).isEqualTo(2)
        assertThat(node.get("endringerIRefusjon")[0].get("endringsdato").toString()).isEqualTo("\"2015-05-05\"")
        assertThat(node.get("endringerIRefusjon")[0].get("beloep").asLong()).isEqualTo(555555555555)
        assertThat(node.get("endringerIRefusjon")[1].get("endringsdato").toString()).isEqualTo("\"2016-06-06\"")
        assertThat(node.get("endringerIRefusjon")[1].get("beloep").asLong()).isEqualTo(666666666666)
        assertThat(node.get("feriePerioder").size()).isEqualTo(2)
        assertThat(node.get("feriePerioder")[0].get("fom").toString()).isEqualTo("\"2017-07-07\"")
        assertThat(node.get("feriePerioder")[0].get("tom").toString()).isEqualTo("\"2018-08-08\"")
        assertThat(node.get("feriePerioder")[1].get("fom").toString()).isEqualTo("\"2019-09-09\"")
        assertThat(node.get("feriePerioder")[1].get("tom").toString()).isEqualTo("\"2020-12-20\"")
        assertThat(node.get("gjenopptakelserNaturalYtelse").size()).isEqualTo(2)
        assertThat(node.get("gjenopptakelserNaturalYtelse")[0].get("naturalytelse").asText()).isEqualTo("BOLIG")
        assertThat(node.get("gjenopptakelserNaturalYtelse")[0].get("fom").toString()).isEqualTo("\"2011-01-01\"")
        assertThat(node.get("gjenopptakelserNaturalYtelse")[0].get("beloepPrMnd").asLong()).isEqualTo(111111111111)
        assertThat(node.get("gjenopptakelserNaturalYtelse")[1].get("naturalytelse").asText()).isEqualTo("KOSTDAGER")
        assertThat(node.get("gjenopptakelserNaturalYtelse")[1].get("fom").toString()).isEqualTo("\"2012-02-02\"")
        assertThat(node.get("gjenopptakelserNaturalYtelse")[1].get("beloepPrMnd").asLong()).isEqualTo(222222222222)
        assertThat(node.get("opphørAvNaturalYtelse").size()).isEqualTo(2)
        assertThat(node.get("opphørAvNaturalYtelse")[0].get("naturalytelse").asText()).isEqualTo("BIL")
        assertThat(node.get("opphørAvNaturalYtelse")[0].get("fom").toString()).isEqualTo("\"2015-05-05\"")
        assertThat(node.get("opphørAvNaturalYtelse")[0].get("beloepPrMnd").asLong()).isEqualTo(555555555555)
        assertThat(
            node.get("opphørAvNaturalYtelse")[1].get("naturalytelse").asText(),
        ).isEqualTo("TILSKUDDBARNEHAGEPLASS")
        assertThat(node.get("opphørAvNaturalYtelse")[1].get("fom").toString()).isEqualTo("\"2016-06-06\"")
        assertThat(node.get("opphørAvNaturalYtelse")[1].get("beloepPrMnd").asLong()).isEqualTo(666666666666)
    }
}
