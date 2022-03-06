package no.nav.syfo.service

import kotlinx.coroutines.runBlocking
import log
import no.nav.syfo.integration.brreg.BrregClient

class BrregService(
    private val brregClient: BrregClient
) {
    var log = log()

    fun hentVirksomhetsNavn(orgnr: String): String {
        return try {
            val virksomhetsNavn = runBlocking {
                brregClient.getVirksomhetsNavn(orgnr)
            }
            log.info("Fant virksomhet:  " + virksomhetsNavn)
            virksomhetsNavn
        } catch (e: RuntimeException) {
            log.error("Klarte ikke å hente virksomhet!", e)
            "Ukjent arbeidsgiver"
        }
    }
}
