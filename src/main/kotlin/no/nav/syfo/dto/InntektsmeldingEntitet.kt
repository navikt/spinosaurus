package no.nav.syfo.dto

import no.nav.helsearbeidsgiver.utils.wrapper.Fnr
import java.time.LocalDate
import java.time.LocalDateTime

data class InntektsmeldingEntitet(
    val uuid: String,
    var aktorId: String,
    val journalpostId: String,
    val orgnummer: String? = null,
    val arbeidsgiverPrivat: String? = null,
    val behandlet: LocalDateTime? = LocalDateTime.now(),
    val fnr: Fnr,
    var data: String? = null,
) {
    var arbeidsgiverperioder: MutableList<ArbeidsgiverperiodeEntitet> = ArrayList()

    fun leggtilArbeidsgiverperiode(
        fom: LocalDate,
        tom: LocalDate,
    ) {
        val periode = ArbeidsgiverperiodeEntitet(fom = fom, tom = tom)
        periode.inntektsmelding = this
        arbeidsgiverperioder.add(periode)
    }
}
