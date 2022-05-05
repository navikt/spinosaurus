package no.nav.syfo.client.saf

import io.ktor.http.HttpStatusCode
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.helse.arbeidsgiver.integrasjoner.AccessTokenProvider
import no.nav.syfo.client.buildHttpClientJson
import no.nav.syfo.client.saf.model.JournalResponse
import no.nav.syfo.domain.JournalStatus
import no.nav.syfo.koin.buildObjectMapper
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SafJournalpostClientTest {

    private val stsClient = mockk<AccessTokenProvider>(relaxed = true)

    val objectMapper = buildObjectMapper()

    private lateinit var client: SafJournalpostClient

    @Test
    fun `Skal hente ut gyldig respons`() {
        client = SafJournalpostClient(buildHttpClientJson(HttpStatusCode.OK, validJson()), "http://localhost", stsClient)
        runBlocking {
            val journalpost = client.getJournalpostMetadata("123")
            Assertions.assertThat(journalpost?.journalstatus).isEqualTo(JournalStatus.MOTTATT)
            Assertions.assertThat(journalpost?.dokumenter!![0].dokumentInfoId).isEqualTo("533122674")
        }
    }

    @Test
    fun `Skal håndtere feil`() {
        client = SafJournalpostClient(buildHttpClientJson(HttpStatusCode.OK, errorJson()), "http://localhost", stsClient)
        runBlocking {
            assertThrows<ErrorException> {
                client.getJournalpostMetadata("123")
            }
        }
    }

    @Test
    fun `Skal håndtere ikke autorisert`() {
        client = SafJournalpostClient(buildHttpClientJson(HttpStatusCode.OK, unauthorizedJson()), "http://localhost", stsClient)
        runBlocking {
            assertThrows<NotAuthorizedException> {
                client.getJournalpostMetadata("123")
            }
        }
    }

    @Test
    fun `Skal lese gyldig JSON`() {
        validJson()
    }

    @Test
    fun `Skal lese error JSON`() {
        errorJson()
    }

    @Test
    fun `Skal lese unauthorized JSON`() {
        unauthorizedJson()
    }

    @Test
    fun `Skal lese not found JSON`() {
        notFoundJson()
    }

    fun getResource(path: String): String {
        return object {}.javaClass.getResource(path).readText()
    }

    fun getJsonFile(path: String): JournalResponse {
        return objectMapper.readValue(getResource(path), JournalResponse::class.java)
    }

    fun validJson(): JournalResponse {
        return getJsonFile("/saf_valid.json")
    }

    fun errorJson(): JournalResponse {
        return getJsonFile("/saf_error.json")
    }

    fun notFoundJson(): JournalResponse {
        return getJsonFile("/saf_not_found.json")
    }

    fun unauthorizedJson(): JournalResponse {
        return getJsonFile("/saf_unauthorized.json")
    }
}
