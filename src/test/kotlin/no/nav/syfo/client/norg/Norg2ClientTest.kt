package no.nav.syfo.client.norg

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.runBlocking
import no.nav.syfo.koin.buildObjectMapper
import no.nav.syfo.util.customObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class Norg2ClientTest {
    @Test
    fun hentAlleArbeidsfordelinger() {
        runBlocking {
            val norgClient = Norg2Client("url", buildHttpClientJson())
            val arbeidsfordelinger = norgClient.hentAlleArbeidsfordelinger(lagRequest(), "123")
            assertThat(arbeidsfordelinger.size).isEqualTo(1)
            assertThat(arbeidsfordelinger[0].enhetNr).isEqualTo("1234")
        }
    }
}

private fun lagRequest(): ArbeidsfordelingRequest =
    ArbeidsfordelingRequest(
        tema = "tema",
        oppgavetype = "INNT",
    )

private fun buildHttpClientJson(): HttpClient =
    HttpClient(MockEngine) {
        expectSuccess = true
        install(ContentNegotiation) {
            register(ContentType.Application.Json, JacksonConverter(customObjectMapper()))
            jackson {
                registerModule(JavaTimeModule())
            }
        }
        engine {
            addHandler {
                respond(
                    buildObjectMapper().writeValueAsString(lagResponse()),
                    headers = headersOf("Content-Type" to listOf(ContentType.Application.Json.toString())),
                    status = HttpStatusCode.OK,
                )
            }
        }
    }

private fun lagResponse(): List<ArbeidsfordelingResponse> =
    listOf(
        ArbeidsfordelingResponse(
            aktiveringsdato = LocalDate.of(2020, 12, 31),
            antallRessurser = 0,
            enhetId = 123456789,
            enhetNr = "1234",
            kanalstrategi = null,
            navn = "NAV Område",
            nedleggelsesdato = null,
            oppgavebehandler = false,
            orgNivaa = "SPESEN",
            orgNrTilKommunaltNavKontor = "",
            organisasjonsnummer = null,
            sosialeTjenester = "",
            status = "Aktiv",
            type = "KO",
            underAvviklingDato = null,
            underEtableringDato = LocalDate.of(2020, 11, 30),
            versjon = 1,
        ),
    )
