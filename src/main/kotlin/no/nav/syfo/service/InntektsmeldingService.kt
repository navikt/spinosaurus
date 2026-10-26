package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding
import no.nav.syfo.dto.InntektsmeldingEntitet
import no.nav.syfo.mapping.tilRefusjonskrav
import no.nav.syfo.mapping.toInntektsmelding
import no.nav.syfo.mapping.toInntektsmeldingEntitet
import no.nav.syfo.repository.InntektsmeldingRepository
import no.nav.syfo.web.api.InntektsmeldingRefusjonskrav
import java.util.UUID

class InntektsmeldingService(
    private val repository: InntektsmeldingRepository,
    private val objectMapper: ObjectMapper,
) {
    fun findByJournalpost(journalpostId: String): InntektsmeldingEntitet? = repository.findByJournalpost(journalpostId)

    fun lagreBehandling(
        inntektsmelding: Inntektsmelding,
        aktorid: String,
    ): InntektsmeldingEntitet {
        val dto = toInntektsmeldingEntitet(inntektsmelding)
        dto.aktorId = aktorid
        dto.data = inntektsmelding.asJsonString(objectMapper)
        return repository.lagreInnteksmelding(dto)
    }

    fun hentRefusjonskrav(inntektsmeldingIder: List<UUID>): List<InntektsmeldingRefusjonskrav> {
        val distinkteInntektsmeldingIder = inntektsmeldingIder.distinct()

        val inntektsmeldingerFunnet =
            repository
                .findByUuids(distinkteInntektsmeldingIder.map { it.toString() })
                .associateBy { it.uuid }

        return distinkteInntektsmeldingIder.mapNotNull { id ->
            inntektsmeldingerFunnet[id.toString()]?.let {
                InntektsmeldingRefusjonskrav(
                    inntektsmeldingId = id,
                    refusjonskrav = toInntektsmelding(it, objectMapper).tilRefusjonskrav(id.toString()),
                )
            }
        }
    }
}

fun Inntektsmelding.asJsonString(objectMapper: ObjectMapper): String = objectMapper.writeValueAsString(this)
