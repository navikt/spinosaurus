package no.nav.syfo.mapping

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.helsearbeidsgiver.utils.pipe.orDefault
import no.nav.syfo.domain.JournalStatus
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding
import no.nav.syfo.dto.InntektsmeldingEntitet

fun toInntektsmelding(
    inntektsmeldingEntitet: InntektsmeldingEntitet,
    objectMapper: ObjectMapper,
): Inntektsmelding {
    val im = objectMapper.readValue(inntektsmeldingEntitet.data, Inntektsmelding::class.java)
    return im.copy(
        fnr = im.fnr.ifEmpty { inntektsmeldingEntitet.fnr.verdi },
        journalStatus =
            im.journalStatus
                .takeIf { it != JournalStatus.MIDLERTIDIG }
                .orDefault(JournalStatus.MOTTATT),
    )
}
