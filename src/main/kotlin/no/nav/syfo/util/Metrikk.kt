package no.nav.syfo.util

import io.prometheus.client.Counter

object Metrikk {
    private val inntektsmeldingSykepengerUtland =
        metrikk(
            "sykepenger_utland",
            "Metrikker for sykepenger utland",
        )
    private val opprettOppgave =
        metrikk(
            "opprett_oppgave",
            "Metrikker for OpprettOppgave",
            "eksisterer",
        )
    private val utsattOppgaveUkjent =
        metrikk(
            "utsatt_oppgave_ukjent",
            "Mottok oppdatering på en ukjent oppgave",
        )
    private val utsattOppgaveUtsett =
        metrikk(
            "utsatt_oppgave_utsett",
            "Oppdaterte timeout for inntektsmelding",
        )
    private val utsattOppgaveForkast =
        metrikk(
            "utsatt_oppgave_forkast",
            "Forkaster oppgave",
        )
    private val utsattOppgaveOpprett =
        metrikk(
            "utsatt_oppgave_opprett",
            "Oppretter oppgave",
        )
    private val utsattOppgaveOpprettTimeout =
        metrikk(
            "utsatt_oppgave_opprett_timeout",
            "Oppretter oppgave pga timeout",
        )
    private val utsattOppgaveIrrelevant =
        metrikk(
            "utsatt_oppgave_irrelevant",
            "Irrelevant dokumentid",
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

private fun metrikk(
    nameSuffix: String,
    helpText: String,
    labelName: String? = null,
): Counter =
    Counter
        .build()
        .namespace("spinn")
        .name("syfoinntektsmelding_$nameSuffix")
        .help(helpText)
        .labelNames(
            *listOfNotNull("type", labelName).toTypedArray(),
        ).register()
