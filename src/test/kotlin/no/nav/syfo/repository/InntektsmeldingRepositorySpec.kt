package no.nav.syfo.repository

import no.nav.helsearbeidsgiver.utils.test.wrapper.genererGyldig
import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import no.nav.inntektsmelding.kontrakt.serde.JacksonJsonConfig
import no.nav.syfo.domain.JournalStatus
import no.nav.syfo.domain.Periode
import no.nav.syfo.domain.inntektsmelding.EndringIRefusjon
import no.nav.syfo.domain.inntektsmelding.GjenopptakelseNaturalytelse
import no.nav.syfo.domain.inntektsmelding.Gyldighetsstatus
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding
import no.nav.syfo.domain.inntektsmelding.Naturalytelse
import no.nav.syfo.domain.inntektsmelding.OpphoerAvNaturalytelse
import no.nav.syfo.domain.inntektsmelding.Refusjon
import no.nav.syfo.dto.InntektsmeldingEntitet
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertNotNull
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InntektsmeldingRepositorySpec : WithPostgresContainer() {
    val repository = InntektsmeldingRepositoryImp(dataSource)

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun findByUuids() {
        val ids = (1..3).map { UUID.randomUUID().toString() }
        ids.forEachIndexed { i, id ->
            repository.lagreInnteksmelding(
                InntektsmeldingEntitet(
                    uuid = id,
                    journalpostId = "jp-uuids-$i",
                    behandlet = LocalDateTime.of(2019, 10, 1, 5, 18, 45, 0),
                    orgnummer = "orgnummer",
                    arbeidsgiverPrivat = "arbeidsgiverPrivat",
                    fnr = Fnr.genererGyldig(),
                    aktorId = "aktorId1",
                    data = "{}",
                ),
            )
        }
        val resultat = repository.findByUuids(listOf(ids[0], ids[2], UUID.randomUUID().toString()))
        assertThat(resultat.map { it.uuid }).containsExactlyInAnyOrder(ids[0], ids[2])
        assertThat(repository.findByUuids(emptyList())).isEmpty()
    }

    @Test
    fun findByJournalpostId() {
        val inntektsmelding =
            InntektsmeldingEntitet(
                uuid = UUID.randomUUID().toString(),
                journalpostId = "jp-123-987",
                behandlet = LocalDateTime.of(2019, 10, 1, 5, 18, 45, 0),
                orgnummer = "orgnummer",
                arbeidsgiverPrivat = "arbeidsgiverPrivat",
                fnr = Fnr.genererGyldig(),
                aktorId = "aktorId1",
            )
        repository.lagreInnteksmelding(inntektsmelding)
        assertNotNull(repository.findByJournalpost("jp-123-987"))
    }

    @Test
    fun lagre_inntektsmelding_som_json() {
        val im =
            Inntektsmelding(
                id = "id-abc",
                fnr = "fnr-123",
                arbeidsgiverOrgnummer = "arb-org-123",
                arbeidsgiverPrivatFnr = "arb-priv-123",
                arbeidsgiverPrivatAktørId = "arb-priv-aktør-123",
                arbeidsforholdId = "arb-123",
                journalpostId = "jp-123",
                arsakTilInnsending = "Ingen årsak",
                journalStatus = JournalStatus.MOTTATT,
                arbeidsgiverperioder =
                    listOf(
                        Periode(fom = LocalDate.of(2011, 11, 1), tom = LocalDate.of(2012, 12, 2)),
                        Periode(fom = LocalDate.of(2013, 3, 3), tom = LocalDate.of(2014, 4, 4)),
                    ),
                beregnetInntekt = BigDecimal(999999999999),
                refusjon = Refusjon(BigDecimal(333333333333), LocalDate.of(2020, 2, 20)),
                endringerIRefusjon =
                    listOf(
                        EndringIRefusjon(LocalDate.of(2015, 5, 5), BigDecimal(555555555555)),
                        EndringIRefusjon(LocalDate.of(2016, 6, 6), BigDecimal(666666666666)),
                    ),
                opphørAvNaturalYtelse =
                    listOf(
                        OpphoerAvNaturalytelse(Naturalytelse.BIL, LocalDate.of(2015, 5, 5), BigDecimal(555555555555)),
                        OpphoerAvNaturalytelse(Naturalytelse.TILSKUDDBARNEHAGEPLASS, LocalDate.of(2016, 6, 6), BigDecimal(666666666666)),
                    ),
                gjenopptakelserNaturalYtelse =
                    listOf(
                        GjenopptakelseNaturalytelse(Naturalytelse.BOLIG, LocalDate.of(2011, 1, 1), BigDecimal(111111111111)),
                        GjenopptakelseNaturalytelse(Naturalytelse.KOSTDAGER, LocalDate.of(2012, 2, 2), BigDecimal(222222222222)),
                    ),
                gyldighetsStatus = Gyldighetsstatus.GYLDIG,
                arkivRefereranse = "ar-123",
                feriePerioder =
                    listOf(
                        Periode(fom = LocalDate.of(2017, 7, 7), tom = LocalDate.of(2018, 8, 8)),
                        Periode(fom = LocalDate.of(2019, 9, 9), tom = LocalDate.of(2020, 12, 20)),
                    ),
                førsteFraværsdag = LocalDate.of(2010, 2, 10),
                mottattDato = LocalDateTime.of(2010, 5, 4, 3, 2, 1),
                sakId = "sak-123",
                aktorId = "aktør-123",
                begrunnelseRedusert = "Grunn til reduksjon",
            )
        val mapper = JacksonJsonConfig.opprettObjectMapper()
        val inntektsmelding =
            InntektsmeldingEntitet(
                uuid = UUID.randomUUID().toString(),
                journalpostId = "journalpostId",
                behandlet = LocalDateTime.now(),
                orgnummer = "orgnummer",
                arbeidsgiverPrivat = "arbeidsgiverPrivat",
                aktorId = "aktorId-repo-test",
                fnr = Fnr.genererGyldig(),
                data = mapper.writeValueAsString(im),
            )
        repository.lagreInnteksmelding(inntektsmelding)
        val i = repository.findByUuid(inntektsmelding.uuid)!!

        val data = mapper.readTree(i.data)

        assertThat(data.get("id").asText()).isEqualTo("id-abc")
        assertThat(data.get("fnr").asText()).isEqualTo("fnr-123")
        assertThat(data.get("aktorId").asText()).isEqualTo("aktør-123")
        assertThat(data.get("journalpostId").asText()).isEqualTo("jp-123")
        assertThat(data.get("refusjon").get("beloepPrMnd").asText()).isEqualTo("333333333333")
        assertThat(data.get("refusjon").get("opphoersdato").asText()).isEqualTo("2020-02-20")
        assertThat(data.get("begrunnelseRedusert").asText()).isEqualTo("Grunn til reduksjon")
        assertThat(data.get("sakId").asText()).isEqualTo("sak-123")
        assertThat(data.get("mottattDato").asText()).isEqualTo("2010-05-04T03:02:01")
        assertThat(data.get("arkivRefereranse").asText()).isEqualTo("ar-123")
        assertThat(data.get("førsteFraværsdag").asText()).isEqualTo("2010-02-10")
        assertThat(data.get("arsakTilInnsending").asText()).isEqualTo("Ingen årsak")
    }
}
