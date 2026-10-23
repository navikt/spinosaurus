package no.nav.syfo.util

import io.prometheus.client.Counter

class Metrikk {
    private val metricsNs = "spinn"
    val counters = HashMap<String, Counter>()

    private fun proseseringsMetrikker(
        metricName: String,
        metricHelpText: String,
        vararg labelNames: String,
    ): Counter =
        if (counters.containsKey(metricName)) {
            counters[metricName]!!
        } else {
            val counter =
                Counter
                    .build()
                    .namespace(metricsNs)
                    .name(metricName)
                    .labelNames(*labelNames)
                    .help(metricHelpText)
                    .register()

            counters[metricName] = counter
            counter
        }

    val inntektsmeldingSykepengerUtland =
        proseseringsMetrikker(
            "syfoinntektsmelding_sykepenger_utland",
            "Metrikker for sykepenger utland",
            "type",
        )

    val arbeidsgiverperioder =
        proseseringsMetrikker(
            "syfoinntektsmelding_arbeidsgiverperioder",
            "Metrikker for arbeidsgiverperioder",
            "type",
            "antall",
        )

    val naturalytelse =
        proseseringsMetrikker(
            "syfoinntektsmelding_faar_naturalytelse",
            "Metrikker for Naturalytelse",
            "type",
        )

    val opprettOppgave =
        proseseringsMetrikker(
            "syfoinntektsmelding_opprett_oppgave",
            "Metrikker for OpprettOppgave",
            "type",
            "eksisterer",
        )

    val utsattOppgaveUkjent =
        proseseringsMetrikker(
            "syfoinntektsmelding_utsatt_oppgave_ukjent",
            "Mottok oppdatering på en ukjent oppgave",
            "type",
        )
    val utsattOppgaveUtsett =
        proseseringsMetrikker(
            "syfoinntektsmelding_utsatt_oppgave_utsett",
            "Oppdaterte timeout for inntektsmelding",
            "type",
        )
    val utsattOppgaveForkast =
        proseseringsMetrikker(
            "syfoinntektsmelding_utsatt_oppgave_forkast",
            "Forkaster oppgave",
            "type",
        )
    val utsattOppgaveOpprett =
        proseseringsMetrikker(
            "syfoinntektsmelding_utsatt_oppgave_opprett",
            "Oppretter oppgave",
            "type",
        )

    val utsattOppgaveOpprettTimeout =
        proseseringsMetrikker(
            "syfoinntektsmelding_utsatt_oppgave_opprett_timeout",
            "Oppretter oppgave pga timeout",
            "type",
        )
    val utsattOppgaveIrrelevant =
        proseseringsMetrikker(
            "syfoinntektsmelding_utsatt_oppgave_irrelevant",
            "Irrelevant dokumentid",
            "type",
        )

    fun tellInntektsmeldingSykepengerUtland() = inntektsmeldingSykepengerUtland.labels("info").inc()

    fun tellOpprettOppgave(eksisterer: Boolean) = opprettOppgave.labels("info", if (eksisterer) "J" else "N").inc()

    fun tellUtsattOppgaveUkjent() = utsattOppgaveUkjent.labels("info").inc()

    fun tellUtsattOppgaveUtsett() = utsattOppgaveUtsett.labels("info").inc()

    fun tellUtsattOppgaveForkast() = utsattOppgaveForkast.labels("info").inc()

    fun tellUtsattOppgaveOpprett() = utsattOppgaveOpprett.labels("info").inc()

    fun tellUtsattOppgaveOpprettTimeout() = utsattOppgaveOpprettTimeout.labels("info").inc()

    fun tellUtsattOppgaveIrrelevant() = utsattOppgaveIrrelevant.labels("info").inc()
}
