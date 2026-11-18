package no.nav.syfo.util

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding

private val objectMapper = customObjectMapper()

object InntektsmeldingJsonMapper {
    fun toJson(inntektsmelding: Inntektsmelding): String = objectMapper.writeValueAsString(inntektsmelding)

    fun fromJson(
        json: String,
        fnr: Fnr,
    ): Inntektsmelding =
        objectMapper
            .readValue<Inntektsmelding>(json)
            .copy(
                fnr = fnr.verdi,
            )
}
