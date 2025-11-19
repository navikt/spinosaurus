package no.nav.syfo.util

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger
import java.net.InetAddress

object LeaderElectionManager {
    val httpClient = createHttpClient(3)
    val electorUrl = System.getenv("ELECTOR_GET_URL")

    fun isLeader() =
        runBlocking {
            try {
                val electedPod: ElectedPod = httpClient.get(electorUrl).body()
                val hostname = InetAddress.getLocalHost().hostName
                hostname == electedPod.name
            } catch (e: Exception) {
                "Feil ved henting av leader status".also {
                    logger().error(it)
                    sikkerLogger().error(it, e)
                }
                false
            }
        }

    @Serializable
    data class ElectedPod(
        val name: String,
    )
}
