package no.nav.syfo.utsattoppgave

import kotlinx.coroutines.runBlocking
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger
import no.nav.syfo.client.oppgave.OppgaveService
import no.nav.syfo.client.oppgave.lagInntektsmeldingOppgaveBeskrivelse
import no.nav.syfo.domain.OppgaveResultat
import no.nav.syfo.domain.inntektsmelding.Inntektsmelding
import no.nav.syfo.dto.Tilstand
import no.nav.syfo.dto.UtsattOppgaveEntitet
import no.nav.syfo.service.BehandlendeEnhetService
import no.nav.syfo.service.InntektsmeldingService
import no.nav.syfo.util.InntektsmeldingJsonMapper
import no.nav.syfo.util.Metrikk
import java.time.LocalDateTime
import java.util.UUID

class UtsattOppgaveService(
    private val utsattOppgaveDAO: UtsattOppgaveDAO,
    private val oppgaveService: OppgaveService,
    private val behandlendeEnhetService: BehandlendeEnhetService,
    private val inntektsmeldingService: InntektsmeldingService,
) {
    private val logger = this.logger()
    private val sikkerlogger = sikkerLogger()

    fun prosesser(oppdatering: OppgaveOppdatering) {
        val oppgave = utsattOppgaveDAO.finn(oppdatering.id.toString())
        if (oppgave == null) {
            Metrikk.tellUtsattOppgaveUkjent()
            logger.info("Mottok oppdatering på en ukjent oppgave for id: ${oppdatering.id}")
            return
        }
        logger.debug("Fant oppgave for inntektsmelding: ${oppgave.arkivreferanse} med tilstand: ${oppgave.tilstand.name}")
        val gjelderSpeil = oppdatering.oppdateringstype.erSpeilRelatert()

        when (oppgave.tilstand to oppdatering.handling) {
            (Tilstand.Utsatt to Handling.Utsett) -> {
                oppdatering.timeout ?: error("Timeout på utsettelse mangler for inntektsmelding: ${oppgave.arkivreferanse}")

                val oppdatertOppgave =
                    oppgave.copy(
                        timeout = oppdatering.timeout,
                        oppdatert = LocalDateTime.now(),
                        speil = gjelderSpeil,
                    )

                utsattOppgaveDAO.oppdater(oppdatertOppgave)

                Metrikk.tellUtsattOppgaveUtsett()
                logger.debug("Oppdaterte timeout på inntektsmelding: ${oppdatertOppgave.arkivreferanse} til ${oppdatertOppgave.timeout}")
            }

            (Tilstand.Utsatt to Handling.Forkast) -> {
                val oppdatertOppgave =
                    oppgave.copy(
                        tilstand = Tilstand.Forkastet,
                        oppdatert = LocalDateTime.now(),
                        speil = gjelderSpeil,
                    )

                utsattOppgaveDAO.oppdater(oppdatertOppgave)

                Metrikk.tellUtsattOppgaveForkast()
                logger.debug("Endret oppgave: ${oppdatertOppgave.arkivreferanse} til tilstand: ${oppdatertOppgave.tilstand.name}")
            }

            (Tilstand.Utsatt to Handling.Opprett),
            (Tilstand.Forkastet to Handling.Opprett),
            -> {
                inntektsmeldingService
                    .hentInntektsmelding(oppgave.inntektsmeldingId)
                    .onSuccess { inntektsmelding ->
                        val gjelderUtland = behandlendeEnhetService.gjelderUtland(oppgave.fnr)
                        val behandlingsKategori = finnBehandlingsKategori(inntektsmelding, oppgave.speil, gjelderUtland)
                        if (behandlingsKategori != BehandlingsKategori.IKKE_FRAVAER) {
                            val resultat = oppgaveService.opprettOppgaveIGosys(oppgave, behandlingsKategori, inntektsmelding)

                            val oppdatertOppgave =
                                oppgave.copy(
                                    tilstand = Tilstand.Opprettet,
                                    oppdatert = LocalDateTime.now(),
                                    speil = gjelderSpeil,
                                    gosysOppgaveId = resultat.oppgaveId.toString(),
                                    utbetalingBruker = resultat.utbetalingBruker,
                                )

                            utsattOppgaveDAO.oppdater(oppdatertOppgave)

                            Metrikk.tellUtsattOppgaveOpprett()
                            logger.info(
                                "Endret oppgave: ${oppdatertOppgave.inntektsmeldingId} til tilstand: ${oppdatertOppgave.tilstand.name} gosys oppgaveID: ${oppdatertOppgave.gosysOppgaveId} duplikat? ${resultat.duplikat}",
                            )
                        } else {
                            if (oppgave.tilstand == Tilstand.Utsatt) {
                                val oppdatertOppgave =
                                    oppgave.copy(
                                        tilstand = Tilstand.Forkastet,
                                        oppdatert = LocalDateTime.now(),
                                        speil = gjelderSpeil,
                                    )

                                utsattOppgaveDAO.oppdater(oppdatertOppgave)

                                sikkerlogger.info(
                                    "Endret oppgave: ${oppdatertOppgave.inntektsmeldingId} til tilstand: ${oppdatertOppgave.tilstand.name} pga behandlingskategori: $behandlingsKategori",
                                )
                            }

                            logger.info("Oppgave blir ikke opprettet for inntektsmeldingId: ${oppgave.inntektsmeldingId}")
                            sikkerlogger.info(
                                "Oppgave blir ikke opprettet for inntektsmeldingId: ${oppgave.inntektsmeldingId}, har behandlingskategori: $behandlingsKategori",
                            )
                        }
                    }.onFailure {
                        logger.error("Feil ved henting av inntektsmelding fra databasen for arkivref: ${oppgave.arkivreferanse}")
                        sikkerlogger.error(it.message, it)
                    }
            }

            else -> {
                Metrikk.tellUtsattOppgaveIrrelevant()
                logger.info("Oppdatering på dokumentId: ${oppdatering.id} ikke relevant")
            }
        }
    }

    fun opprett(utsattOppgave: UtsattOppgaveEntitet) {
        utsattOppgaveDAO.opprett(utsattOppgave)
    }
}

fun InntektsmeldingService.hentInntektsmelding(inntektsmeldingId: String): Result<Inntektsmelding> {
    val inntektsmeldingDto = this.findById(inntektsmeldingId.let(UUID::fromString))
    return if (inntektsmeldingDto != null) {
        Result.success(InntektsmeldingJsonMapper.fromJson(inntektsmeldingDto.data, inntektsmeldingDto.fnr))
    } else {
        Result.failure(Exception("Fant ikke inntektsmelding for ID '$inntektsmeldingId'."))
    }
}

fun OppgaveService.opprettOppgaveIGosys(
    utsattOppgave: UtsattOppgaveEntitet,
    behandlingsKategori: BehandlingsKategori,
    inntektsmelding: Inntektsmelding,
): OppgaveResultat =
    runBlocking {
        opprettOppgave(
            journalpostId = utsattOppgave.journalpostId,
            aktoerId = utsattOppgave.aktørId,
            behandlingsKategori = behandlingsKategori,
            oppgaveBeskrivelse = lagInntektsmeldingOppgaveBeskrivelse(inntektsmelding, behandlingsKategori),
        )
    }

class OppgaveOppdatering(
    val id: UUID,
    val handling: Handling,
    val timeout: LocalDateTime?,
    val oppdateringstype: OppdateringstypeDTO,
)

enum class Handling {
    Utsett,
    Opprett,
    Forkast,
}
