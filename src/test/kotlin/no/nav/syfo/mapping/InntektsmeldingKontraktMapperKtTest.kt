package no.nav.syfo.mapping

import no.nav.inntektsmeldingkontrakt.Arbeidsgivertype
import no.nav.syfo.FØRSTE_FEBRUAR
import no.nav.syfo.FØRSTE_JANUAR
import no.nav.syfo.domain.JournalStatus
import no.nav.syfo.domain.inntektsmelding.Gyldighetsstatus
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding
import no.nav.syfo.domain.inntektsmelding.Refusjon
import no.nav.syfo.dto.InntektsmeldingEntitet
import no.nav.syfo.grunnleggendeInntektsmelding
import no.nav.syfo.koin.buildObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

class InntektsmeldingKontraktMapperKtTest {

    val om = buildObjectMapper()

    @Test
    fun toInntektsmeldingDTO() {
        val inntektsmelding = Inntektsmelding(
            fnr = "fnr",
            arbeidsgiverPrivatFnr = "fnr",
            journalpostId = "journalpostId",
            arsakTilInnsending = "",
            journalStatus = JournalStatus.MOTTATT,
            arbeidsgiverperioder = ArrayList(),
            refusjon = Refusjon(BigDecimal.ONE),
            gyldighetsStatus = Gyldighetsstatus.GYLDIG,
            arkivRefereranse = "ar123",
            førsteFraværsdag = LocalDate.now(),
            mottattDato = LocalDateTime.of(2019, 10, 1, 5, 18, 45, 0),
            sakId = "sakId",
            aktorId = "aktorId"
        )
        val dto = toInntektsmeldingEntitet(inntektsmelding)
        assertThat(dto.journalpostId).isEqualTo("journalpostId")
        assertThat(dto.aktorId).isEqualTo("aktorId")
        assertThat(dto.sakId).isEqualTo("sakId")
        assertThat(dto.arbeidsgiverPrivat).isEqualTo("fnr")
        assertThat(dto.uuid).isNotNull
        assertThat(dto.behandlet).isEqualTo(LocalDateTime.of(2019, 10, 1, 5, 18, 45, 0))
        assertThat(dto.arbeidsgiverperioder.size).isEqualTo(0)
    }

    @Test
    fun toInntektsmelding() {
        val dto = InntektsmeldingEntitet(
            journalpostId = grunnleggendeInntektsmelding.journalpostId,
            behandlet = LocalDateTime.of(2019, 10, 25, 0, 0, 0, 0),
            sakId = grunnleggendeInntektsmelding.sakId!!,
            orgnummer = grunnleggendeInntektsmelding.arbeidsgiverOrgnummer,
            arbeidsgiverPrivat = "arbeidsgiverPrivat",
            aktorId = grunnleggendeInntektsmelding.aktorId.toString(),
            data = om.writeValueAsString(grunnleggendeInntektsmelding)
        )
        val i = toInntektsmelding(dto, om)
        assertThat(i.journalpostId).isEqualTo("123")
        assertThat(i.sakId).isEqualTo("sakId")
        assertThat(i.arbeidsgiverOrgnummer).isEqualTo("1234")
        assertThat(i.fnr).isEqualTo(grunnleggendeInntektsmelding.fnr)
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
                grunnleggendeInntektsmelding.copy(arbeidsgiverOrgnummer = null, arbeidsgiverPrivatFnr = "00")
            )
        ).isEqualTo(Arbeidsgivertype.PRIVAT)
    }
}
