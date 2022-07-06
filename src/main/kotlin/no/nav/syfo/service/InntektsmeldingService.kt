package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding
import no.nav.syfo.dto.InntektsmeldingEntitet
import no.nav.syfo.mapping.toInntektsmelding
import no.nav.syfo.mapping.toInntektsmeldingEntitet
import no.nav.syfo.repository.InntektsmeldingRepository

class InntektsmeldingService(
    private val repository: InntektsmeldingRepository,
    private val objectMapper: ObjectMapper
) {
    fun finnBehandledeInntektsmeldinger(aktoerId: String): List<Inntektsmelding> {
        return repository.findByAktorId(aktoerId).map { toInntektsmelding(it, objectMapper) }
    }

    fun isDuplicate(inntektsmelding: Inntektsmelding): Boolean {
        if (inntektsmelding.aktorId == null) {
            return false
        }
        return isDuplicateWithLatest(inntektsmelding, finnBehandledeInntektsmeldinger(inntektsmelding.aktorId!!))
    }

    fun lagreBehandling(
        inntektsmelding: Inntektsmelding,
        aktorid: String,
        arkivReferanse: String
    ): InntektsmeldingEntitet {
        val dto = toInntektsmeldingEntitet(inntektsmelding)
        dto.aktorId = aktorid
        dto.data = inntektsmelding.asJsonString(objectMapper)
        return repository.lagreInnteksmelding(dto)
    }
}

fun Inntektsmelding.asJsonString(objectMapper: ObjectMapper): String {
    val im = this.copy(fnr = "") // La stå! Ikke lagre fødselsnummer
    return objectMapper.writeValueAsString(im)
}

fun isDuplicateWithLatest(inntektsmelding: Inntektsmelding, list: List<Inntektsmelding>): Boolean {
    if (list.isEmpty()) {
        return false
    }
    return inntektsmelding.isDuplicate(list.sortedBy { it.innsendingstidspunkt }.last())
}
