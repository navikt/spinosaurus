package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding
import no.nav.syfo.dto.InntektsmeldingEntitet
import no.nav.syfo.mapping.mapInntektsmeldingKontrakt
import no.nav.syfo.mapping.toInntektsmelding
import no.nav.syfo.mapping.toInntektsmeldingEntitet
import no.nav.syfo.repository.InntektsmeldingRepository
import no.nav.syfo.util.validerInntektsmelding
import no.nav.syfo.web.api.FinnInntektsmeldingerRequest
import org.slf4j.Logger

class InntektsmeldingService(
    private val repository: InntektsmeldingRepository,
    private val objectMapper: ObjectMapper,
) {
    private val logger = this.logger()

    fun finnBehandledeInntektsmeldinger(aktoerId: String): List<Inntektsmelding> = repository.findByAktorId(aktoerId).map { toInntektsmelding(it, objectMapper) }

    fun findByJournalpost(journalpostId: String): InntektsmeldingEntitet? = repository.findByJournalpost(journalpostId)

    fun isDuplicate(inntektsmelding: Inntektsmelding): Boolean {
        val aktorId = inntektsmelding.aktorId
        return aktorId != null &&
            isDuplicateWithLatest(logger, inntektsmelding, finnBehandledeInntektsmeldinger(aktorId))
    }

    fun lagreBehandling(
        inntektsmelding: Inntektsmelding,
        aktorid: String,
    ): InntektsmeldingEntitet {
        val dto = toInntektsmeldingEntitet(inntektsmelding)
        dto.aktorId = aktorid
        dto.data = inntektsmelding.asJsonString(objectMapper)
        return repository.lagreInnteksmelding(dto)
    }

    fun finnInntektsmeldinger(request: FinnInntektsmeldingerRequest): List<no.nav.inntektsmeldingkontrakt.Inntektsmelding> {
        val results = repository.findByFnrInPeriod(request.fnr, request.fom, request.tom)
        return if (results.isEmpty()) {
            logger().info("Fant ingen inntektsmeldinger!")
            emptyList()
        } else {
            results.map { dto ->
                val inntektsmelding = toInntektsmelding(dto, objectMapper)
                mapInntektsmeldingKontrakt(
                    inntektsmelding,
                    dto.aktorId,
                    validerInntektsmelding(inntektsmelding),
                    inntektsmelding.arkivRefereranse,
                    dto.uuid,
                )
            }
        }
    }
}

fun Inntektsmelding.asJsonString(objectMapper: ObjectMapper): String = objectMapper.writeValueAsString(this)

fun isDuplicateWithLatest(
    logger: Logger,
    inntektsmelding: Inntektsmelding,
    list: List<Inntektsmelding>,
): Boolean {
    val nyesteInntektsmelding = list.maxByOrNull { it.mottattDato }
    return if (nyesteInntektsmelding == null) {
        false
    } else {
        val duplikatLatest = inntektsmelding.isDuplicate(nyesteInntektsmelding)
        val duplikatExclusive = inntektsmelding.isDuplicateExclusiveArsakInnsending(nyesteInntektsmelding)

        // Hvis AG angir en "Endring", blir dette videresendt til Spleis per nå, selv om innholdet er kliss likt. Logger til info for å vite faktisk omfang.
        if (!duplikatLatest && duplikatExclusive) {
            logger.warn("Likhetssjekk: Er duplikat ekslusiv ÅrsakInnsending! Journalpost: ${inntektsmelding.journalpostId} ")
        }

        duplikatLatest
    }
}
