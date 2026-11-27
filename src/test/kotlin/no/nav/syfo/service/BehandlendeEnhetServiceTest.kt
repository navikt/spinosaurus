package no.nav.syfo.service

import io.mockk.coEvery
import io.mockk.mockk
import no.nav.helsearbeidsgiver.pdl.PdlClient
import no.nav.helsearbeidsgiver.pdl.domene.FullPerson
import no.nav.helsearbeidsgiver.pdl.domene.PersonNavn
import no.nav.helsearbeidsgiver.utils.test.date.januar
import no.nav.helsearbeidsgiver.utils.test.wrapper.genererGyldig
import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import no.nav.syfo.client.norg2.Norg2Client
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BehandlendeEnhetServiceTest {
    val pdlClient = mockk<PdlClient>()
    val norg2Client = mockk<Norg2Client>()

    val behandlendeEnhetService = BehandlendeEnhetService(pdlClient, norg2Client)

    @Test
    fun `behandlende enhet gjelder utland`() {
        coEvery { pdlClient.fullPerson(mockFnr) } returns mockPerson
        coEvery { norg2Client.hentBehandlendeEnhet(any(), any()) } returns "4474"

        val gjelderUtland = behandlendeEnhetService.gjelderUtland(mockFnr)
        Assertions.assertThat(gjelderUtland).isEqualTo(true)
    }

    @Test
    fun `behandlende enhet gjelder _ikke_ utland`() {
        coEvery { pdlClient.fullPerson(mockFnr) } returns mockPerson
        coEvery { norg2Client.hentBehandlendeEnhet(any(), any()) } returns "0123"

        val gjelderUtland = behandlendeEnhetService.gjelderUtland(mockFnr)
        Assertions.assertThat(gjelderUtland).isEqualTo(false)
    }

    @Test
    fun `finner ikke behandlende enhet`() {
        coEvery { pdlClient.fullPerson(mockFnr) } returns mockPerson
        coEvery { norg2Client.hentBehandlendeEnhet(any(), any()) } returns null

        assertThrows<RuntimeException> {
            behandlendeEnhetService.gjelderUtland(mockFnr)
        }
    }
}

private val mockFnr = Fnr.genererGyldig().toString()
private val mockPerson =
    FullPerson(
        navn = PersonNavn("Jeppe", "På", "Bjerget"),
        foedselsdato = 1.januar(1986),
        ident = mockFnr,
        diskresjonskode = "SPFO",
        geografiskTilknytning = "1851",
    )
