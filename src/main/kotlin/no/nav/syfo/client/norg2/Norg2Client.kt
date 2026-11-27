package no.nav.syfo.client.norg2

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.withCharset
import no.nav.helsearbeidsgiver.utils.log.MdcUtils

/**
 * Klient som henter alle arbeidsfordelinger
 *
 * Dokumentasjon
 * https://confluence.adeo.no/pages/viewpage.action?pageId=178072651
 *
 * Swagger
 * https://norg2.dev.adeo.no/norg2/swagger-ui.html#/arbeidsfordeling/findArbeidsfordelingByCriteriaUsingPOST
 *
 */
class Norg2Client(
    private val url: String,
    private val httpClient: HttpClient,
) {
    /**
     * Oppslag av informasjon om ruting av arbeidsoppgaver til enheter.
     */
    suspend fun hentBehandlendeEnhet(
        diskresjonskode: String?,
        geografiskOmraade: String?,
    ): String? {
        val callId = MdcUtils.getCallId()
        val request =
            ArbeidsfordelingRequest(
                tema = "SYK",
                diskresjonskode = diskresjonskode,
                geografiskOmraade = geografiskOmraade,
            )

        val arbeidsfordelinger =
            httpClient
                .post(url) {
                    contentType(ContentType.Application.Json.withCharset(Charsets.UTF_8))
                    header("X-Correlation-ID", callId)
                    header("Nav-Call-Id", callId)
                    header("Nav-Consumer-Id", "spinosaurus")
                    setBody(request)
                }.body<List<ArbeidsfordelingResponse>>()

        return arbeidsfordelinger.firstNotNullOfOrNull { it.enhetNr }
    }
}

private data class ArbeidsfordelingRequest(
    val tema: String,
    val diskresjonskode: String? = null,
    val geografiskOmraade: String? = null,
)

private data class ArbeidsfordelingResponse(
    val enhetNr: String? = null,
)
