package no.nav.syfo.client.norg2

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import no.nav.helsearbeidsgiver.utils.test.json.removeJsonWhitespace
import no.nav.syfo.util.configure
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class Norg2ClientTest {
    @Test
    fun `hent behandlende enhet`() {
        runBlocking {
            val norg2Client = Norg2Client("url", mockClient(responseJsonMedEnhet))

            val behandlendeEnhet =
                norg2Client.hentBehandlendeEnhet(
                    diskresjonskode = "Cøliaki",
                    geografiskOmraade = "Narnia",
                )

            assertThat(behandlendeEnhet).isEqualTo("1234")
        }
    }

    @Test
    fun `finner ikke behandlende enhet`() {
        runBlocking {
            val norg2Client = Norg2Client("url", mockClient("[]"))

            val behandlendeEnhet =
                norg2Client.hentBehandlendeEnhet(
                    diskresjonskode = "Cøliaki",
                    geografiskOmraade = "Narnia",
                )

            assertThat(behandlendeEnhet).isEqualTo(null)
        }
    }
}

private fun mockClient(responseJson: String): HttpClient {
    val mockEngine =
        MockEngine.create {
            reuseHandlers = false
            requestHandlers.add {
                respond(
                    content = responseJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
                )
            }
        }

    return HttpClient(mockEngine) { configure() }
}

private val responseJsonMedEnhet =
    """
    [
        {
            "aktiveringsdato": "2020-12-31",
            "antallRessurser": 0,
            "enhetId": 123456789,
            "enhetNr": "1234",
            "kanalstrategi": null,
            "navn": "NAV Område",
            "nedleggelsesdato": null,
            "oppgavebehandler": false,
            "orgNivaa": "SPESEN",
            "orgNrTilKommunaltNavKontor": "",
            "organisasjonsnummer": null,
            "sosialeTjenester": "",
            "status": "Aktiv",
            "type": "KO",
            "underAvviklingDato": null,
            "underEtableringDato": "2020-11-30",
            "versjon": 1
        }
    ]
    """.removeJsonWhitespace()
