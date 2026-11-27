package no.nav.syfo.client.auth

import com.fasterxml.jackson.annotation.JsonProperty
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.forms.submitForm
import io.ktor.http.parameters
import kotlinx.coroutines.runBlocking
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger

class AuthClient(
    private val tokenEndpoint: String,
    private val httpClient: HttpClient,
) {
    private val logger = logger()
    private val sikkerLogger = sikkerLogger()

    fun hentAzureToken(target: String): String =
        runBlocking {
            try {
                httpClient
                    .submitForm(
                        tokenEndpoint,
                        parameters {
                            set("target", target)
                            set("identity_provider", IDENTITY_PROVIDER_AZURE_AD)
                        },
                    ).body<ResponseSuccess>()
                    .accessToken
            } catch (e: ResponseException) {
                val tokenError = e.response.body<ResponseError>()
                "Klarte ikke hente token. status='${e.response.status}'".also {
                    logger.error(it)
                    sikkerLogger.error("$it feilbeskrivelse='${tokenError.errorDescription}'")
                }
                throw e
            }
        }
}

private const val IDENTITY_PROVIDER_AZURE_AD = "azuread"

private data class ResponseSuccess(
    @JsonProperty("access_token")
    val accessToken: String,
)

private data class ResponseError(
    @JsonProperty("error_description")
    val errorDescription: String,
)
