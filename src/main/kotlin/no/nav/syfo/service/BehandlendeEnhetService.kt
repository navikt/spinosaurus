package no.nav.syfo.service

import kotlinx.coroutines.runBlocking
import no.nav.helsearbeidsgiver.pdl.PdlClient
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger
import no.nav.helsearbeidsgiver.utils.pipe.ifTrue
import no.nav.syfo.client.norg2.Norg2Client
import no.nav.syfo.util.Metrikk

private const val SYKEPENGER_UTLAND = "4474"

class BehandlendeEnhetService(
    private val pdlClient: PdlClient,
    private val norg2Client: Norg2Client,
) {
    private val logger = logger()
    private val sikkerLogger = sikkerLogger()

    fun gjelderUtland(fnr: String): Boolean {
        val behandlendeEnhet = hentBehandlendeEnhet(fnr)
        return if (behandlendeEnhet != null) {
            "Hentet behandlende enhet '$behandlendeEnhet'.".also {
                logger.debug(it)
                sikkerLogger.debug(it)
            }

            (behandlendeEnhet == SYKEPENGER_UTLAND)
                .ifTrue { Metrikk.tellInntektsmeldingSykepengerUtland() }
        } else {
            val feilmelding = "Klarte ikke hente behandlende enhet."
            logger.error(feilmelding)
            sikkerLogger.error(feilmelding)
            throw RuntimeException(feilmelding, null)
        }
    }

    private fun hentBehandlendeEnhet(fnr: String): String? =
        runBlocking {
            val person = pdlClient.fullPerson(fnr)

            norg2Client.hentBehandlendeEnhet(
                diskresjonskode = person?.diskresjonskode,
                geografiskOmraade = person?.geografiskTilknytning,
            )
        }
}
