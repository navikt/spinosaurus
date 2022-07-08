package no.nav.syfo.client.aktor

import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import kotlinx.coroutines.runBlocking
import no.nav.helsearbeidsgiver.utils.MdcUtils
import no.nav.helsearbeidsgiver.utils.logger
import no.nav.syfo.behandling.AktørKallResponseException
import no.nav.syfo.behandling.FantIkkeAktørException
import no.nav.syfo.client.TokenConsumer
import java.net.ConnectException

class AktorClient(
    private val tokenConsumer: TokenConsumer,
    private val username: String,
    private val endpointUrl: String,
    private val httpClient: HttpClient
) {
    private val logger = this.logger()

    fun getAktorId(fnr: String): String {
        return getIdent(fnr, "AktoerId")
    }

    private fun getIdent(sokeIdent: String, identgruppe: String): String {
        var aktor: Aktor? = null

        runBlocking {
            val urlString = "$endpointUrl/identer?gjeldende=true&identgruppe=$identgruppe"
            try {
                aktor = httpClient.get<AktorResponse> {
                    url(urlString)
                    header("Authorization", "Bearer ${tokenConsumer.token}")
                    header("Nav-Call-Id", MdcUtils.getCallId())
                    header("Nav-Consumer-Id", username)
                    header("Nav-Personidenter", sokeIdent)
                }[sokeIdent]
            } catch (cause: ClientRequestException) {
                val status = cause.response.status.value
                logger.error("Kall mot aktørregister på $endpointUrl feiler med HTTP-$status")
                throw AktørKallResponseException(status, null)
            } catch (cause: ConnectException) {
                logger.error("Kall til $urlString gir ${cause.message}")
                throw AktørKallResponseException(999, cause)
            }
            if (aktor?.identer == null) {
                logger.error("Fant ikke aktøren: ${aktor?.feilmelding}")
                throw FantIkkeAktørException(null)
            }
        }
        return aktor?.identer?.firstOrNull()?.ident.toString()
    }
}
