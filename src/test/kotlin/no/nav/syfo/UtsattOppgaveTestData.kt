package no.nav.syfo

import no.nav.helsearbeidsgiver.utils.test.wrapper.genererGyldig
import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import no.nav.syfo.dto.InntektsmeldingEntitet
import no.nav.syfo.dto.Tilstand
import no.nav.syfo.dto.UtsattOppgaveEntitet
import no.nav.syfo.repository.buildIM
import no.nav.syfo.util.InntektsmeldingJsonMapper
import java.time.LocalDateTime
import java.util.UUID

object UtsattOppgaveTestData {
    const val FNR = "fnr"
    const val AKTOER_ID = "aktørId"
    const val JOURNALPOST_ID = "journalpostId"
    const val ARKIVREFERANSE = "123"

    val timeout: LocalDateTime = LocalDateTime.of(2023, 4, 6, 9, 0)
    val oppgave =
        UtsattOppgaveEntitet(
            fnr = FNR,
            aktørId = AKTOER_ID,
            journalpostId = JOURNALPOST_ID,
            arkivreferanse = ARKIVREFERANSE,
            timeout = timeout,
            inntektsmeldingId = UUID.randomUUID().toString(),
            tilstand = Tilstand.Utsatt,
            gosysOppgaveId = null,
            oppdatert = null,
            speil = false,
            utbetalingBruker = false,
        )

    val inntektsmeldingEntitet =
        InntektsmeldingEntitet(
            uuid = UUID.randomUUID().toString(),
            aktorId = "aktoerid-123",
            fnr = Fnr.genererGyldig(),
            data = InntektsmeldingJsonMapper.toJson(buildIM()),
        )
    val inntektsmeldingEntitetIkkeFravaer =
        inntektsmeldingEntitet.copy(
            data = InntektsmeldingJsonMapper.toJson(buildIM().copy(begrunnelseRedusert = "IkkeFravaer")),
        )
}
