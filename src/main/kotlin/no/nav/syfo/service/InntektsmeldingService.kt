package no.nav.syfo.service

import no.nav.syfo.dto.InntektsmeldingEntitet
import no.nav.syfo.mapping.tilRefusjonskrav
import no.nav.syfo.repository.InntektsmeldingRepository
import no.nav.syfo.util.InntektsmeldingJsonMapper
import no.nav.syfo.web.api.InntektsmeldingRefusjonskrav
import java.util.UUID
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Inntektsmelding as InntektsmeldingFraSimba

class InntektsmeldingService(
    private val repository: InntektsmeldingRepository,
) {
    fun findById(inntektsmeldingId: UUID): InntektsmeldingEntitet? = repository.findById(inntektsmeldingId)

    fun lagreBehandling(
        inntektsmeldingFraSimba: InntektsmeldingFraSimba,
        aktorId: String,
        journalpostId: String,
        inntektsmeldingJson: String,
    ) {
        val dto =
            InntektsmeldingEntitet(
                uuid = inntektsmeldingFraSimba.id,
                aktorId = aktorId,
                fnr = inntektsmeldingFraSimba.sykmeldt.fnr,
                data = inntektsmeldingJson,
            )

        repository.lagreInntektsmelding(
            inntektsmelding = dto,
            journalpostId = journalpostId,
            orgnr = inntektsmeldingFraSimba.avsender.orgnr.verdi,
            behandlet = inntektsmeldingFraSimba.mottatt.toLocalDateTime(),
        )
    }

    fun hentRefusjonskrav(inntektsmeldingIder: Set<UUID>): List<InntektsmeldingRefusjonskrav> =
        repository
            .findByIds(inntektsmeldingIder)
            .associateBy { it.uuid }
            .map {
                val inntektsmelding = InntektsmeldingJsonMapper.fromJson(it.value.data, it.value.fnr)

                InntektsmeldingRefusjonskrav(
                    inntektsmeldingId = it.key,
                    refusjonskrav =
                        tilRefusjonskrav(
                            refusjon = inntektsmelding.refusjon,
                            endringerIRefusjon = inntektsmelding.endringerIRefusjon,
                            inntektsmeldingId = it.key.toString(),
                        ),
                )
            }
}
