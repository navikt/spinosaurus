package no.nav.syfo.service

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helsearbeidsgiver.pdl.PdlClient
import no.nav.helsearbeidsgiver.pdl.domene.FullPerson
import no.nav.helsearbeidsgiver.pdl.domene.PersonNavn
import no.nav.helsearbeidsgiver.utils.test.date.januar
import no.nav.syfo.behandling.IngenAktivEnhetException
import no.nav.syfo.client.norg.ArbeidsfordelingResponse
import no.nav.syfo.client.norg.Norg2Client
import no.nav.syfo.dto.Tilstand
import no.nav.syfo.dto.UtsattOppgaveEntitet
import no.nav.syfo.util.Metrikk
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.time.LocalDate

class BehandlendeEnhetConsumerTest {
    val pdlClient = mockk<PdlClient>(relaxed = true)
    val norg2Client = mockk<Norg2Client>(relaxed = true)
    val metrikk = mockk<Metrikk>(relaxed = true)

    val fnr = "123456789"
    val uuid = "abcdefgh"
    val enhetNr = "enhet_nr_007"

    @Test
    fun skal_finne_enhetsnr_gjelder_utland() {
        coEvery {
            pdlClient.fullPerson(fnr)
        } returns mockFullPerson()
        val arbeidsfordelinger =
            listOf(buildArbeidsfordelingResponse(SYKEPENGER_UTLAND, LocalDate.of(2000, 1, 1), LocalDate.of(2030, 1, 1)))
        every {
            runBlocking {
                norg2Client.hentAlleArbeidsfordelinger(any(), any())
            }
        } returns arbeidsfordelinger
        val enhet = BehandlendeEnhetConsumer(pdlClient, norg2Client, metrikk).gjelderUtland(mockUtsattOppgave())
        Assertions.assertThat(enhet).isEqualTo(true)
    }

    @Test
    fun skal_finne_aktiv_arbeidsfordeling() {
        val fordelinger = listOf(buildArbeidsfordelingResponse(enhetNr, LocalDate.of(2021, 1, 1), LocalDate.of(2022, 1, 1), "Aktiv"))
        val enhet = finnAktivBehandlendeEnhet(fordelinger, "")
        Assertions.assertThat(enhet).isEqualTo(enhetNr)
    }

    @Test
    fun skal_ikke_finne_aktiv_arbeidsfordeling() {
        val fordelinger = emptyList<ArbeidsfordelingResponse>()
        assertThatThrownBy {
            finnAktivBehandlendeEnhet(fordelinger, "")
        }.isInstanceOf(IngenAktivEnhetException::class.java)
    }

    private fun mockFullPerson() =
        FullPerson(
            navn = PersonNavn("Jeppe", "På", "Bjerget"),
            foedselsdato = 1.januar(1986),
            ident = fnr,
            diskresjonskode = "SPFO",
            geografiskTilknytning = "1851",
        )

    private fun mockUtsattOppgave(): UtsattOppgaveEntitet =
        UtsattOppgaveEntitet(
            id = 0,
            inntektsmeldingId = uuid,
            arkivreferanse = "",
            fnr = fnr,
            aktørId = "",
            journalpostId = "",
            timeout = 1.januar.atStartOfDay(),
            tilstand = Tilstand.Opprettet,
            enhet = "",
            gosysOppgaveId = null,
            oppdatert = null,
            speil = true,
            utbetalingBruker = true,
        )
}
