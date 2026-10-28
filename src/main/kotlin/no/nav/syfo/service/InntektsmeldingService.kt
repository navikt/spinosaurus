package no.nav.syfo.service

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.syfo.dto.InntektsmeldingEntitet
import no.nav.syfo.mapping.tilRefusjonskrav
import no.nav.syfo.mapping.toInntektsmelding
import no.nav.syfo.repository.InntektsmeldingRepository
import no.nav.syfo.web.api.InntektsmeldingRefusjonskrav
import java.util.UUID
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Inntektsmelding as InntektsmeldingFraSimba

class InntektsmeldingService(
    private val repository: InntektsmeldingRepository,
    private val objectMapper: ObjectMapper,
) {
    fun findByJournalpost(journalpostId: String): InntektsmeldingEntitet? = repository.findByJournalpost(journalpostId)

    fun lagreBehandling(
        inntektsmeldingFraSimba: InntektsmeldingFraSimba,
        aktorId: String,
        journalpostId: String,
        inntektsmeldingJson: String,
    ) {
        val dto =
            InntektsmeldingEntitet(
                uuid = inntektsmeldingFraSimba.id.toString(),
                aktorId = aktorId,
                fnr = inntektsmeldingFraSimba.sykmeldt.fnr,
                data = inntektsmeldingJson,
            )

        repository.lagreInnteksmelding(
            innteksmelding = dto,
            journalpostId = journalpostId,
            orgnr = inntektsmeldingFraSimba.avsender.orgnr.verdi,
            behandlet = inntektsmeldingFraSimba.mottatt.toLocalDateTime(),
        )
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
