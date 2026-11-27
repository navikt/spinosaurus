package no.nav.syfo.client.leaderelection

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger
import java.net.InetAddress

class LeaderElectionManager(
    private val url: String,
    private val httpClient: HttpClient,
) {
    fun isLeader(): Boolean =
        try {
            val electedPod =
                runBlocking {
                    httpClient.get(url).body<ElectedPod>()
                }

            electedPod.name == InetAddress.getLocalHost().hostName
        } catch (e: Exception) {
            "Feil ved henting av leader status".also {
                logger().error(it)
                sikkerLogger().error(it, e)
            }
            false
        }

    @Serializable
    private data class ElectedPod(
        val name: String,
    )
}
