package no.nav.syfo.simba

import no.nav.helse.arbeidsgiver.integrasjoner.pdl.PdlClient
import no.nav.helse.arbeidsgiver.kubernetes.LivenessComponent
import no.nav.helse.arbeidsgiver.kubernetes.ReadynessComponent
import no.nav.helse.arbeidsgiver.utils.logger
import no.nav.helsearbeidsgiver.domene.inntektsmelding.Inntektsmelding
import no.nav.helsearbeidsgiver.domene.inntektsmelding.JournalfoertInntektsmelding
import no.nav.helsearbeidsgiver.utils.json.fromJson
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger
import no.nav.syfo.behandling.FantIkkeAktørException
import no.nav.syfo.behandling.OPPRETT_OPPGAVE_FORSINKELSE
import no.nav.syfo.dto.Tilstand
import no.nav.syfo.dto.UtsattOppgaveEntitet
import no.nav.syfo.mapping.mapInntektsmeldingKontrakt
import no.nav.syfo.producer.InntektsmeldingAivenProducer
import no.nav.syfo.service.InntektsmeldingService
import no.nav.syfo.util.getAktørid
import no.nav.syfo.util.validerInntektsmelding
import no.nav.syfo.utsattoppgave.UtsattOppgaveService
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.time.LocalDateTime

class InntektsmeldingConsumer(
    props: Map<String, Any>,
    topicName: String,
    private val inntektsmeldingService: InntektsmeldingService,
    private val inntektsmeldingAivenProducer: InntektsmeldingAivenProducer,
    private val utsattOppgaveService: UtsattOppgaveService,
    private val pdlClient: PdlClient
) : ReadynessComponent, LivenessComponent {

    private val consumer = KafkaConsumer<String, String>(props)
    private var ready = false
    private var error = false
    private val log = logger()
    private val sikkerlogger = sikkerLogger()

    init {
        log.info("Lytter på topic $topicName")
        consumer.subscribe(listOf(topicName))
    }

    private fun setIsReady(ready: Boolean) {
        this.ready = ready
    }

    private fun setIsError(isError: Boolean) {
        this.error = isError
    }

    fun start() {
        sikkerlogger.info("Starter InntektsmeldingConsumer...")
        consumer.use {
            setIsReady(true)
            while (!error) {
                it
                    .poll(Duration.ofMillis(1000))
                    .forEach { record ->
                        try {
                            val value = record.value()
                            sikkerlogger.info("InntektsmeldingConsumer: Mottar record fra Simba: $value")
                            val meldingFraSimba = value.fromJson(JournalfoertInntektsmelding.serializer())

                            val im = inntektsmeldingService.findByJournalpost(meldingFraSimba.journalpostId)
                            if (im != null) {
                                sikkerlogger.info("InntektsmeldingConsumer: Behandler ikke ${meldingFraSimba.journalpostId}. Finnes allerede.")
                            } else {
                                behandle(meldingFraSimba.journalpostId, meldingFraSimba.inntektsmelding)
                                log.info("InntektsmeldingConsumer: Behandlet inntektsmelding med journalpostid: ${meldingFraSimba.journalpostId}")
                            }
                            it.commitSync()
                        } catch (e: Throwable) {
                            sikkerlogger.error("InntektsmeldingConsumer: Klarte ikke behandle: ${record.value()}", e)
                            log.error("InntektsmeldingConsumer: Klarte ikke behandle hendelse. Stopper lytting!", e)
                            setIsError(true)
                        }
                    }
            }
        }
    }

    private fun behandle(journalpostId: String, inntektsmeldingFraSimba: Inntektsmelding) {
        val aktorid = pdlClient.getAktørid(inntektsmeldingFraSimba.identitetsnummer)
        val arkivreferanse = "im_$journalpostId"
        if (aktorid == null) {
            log.error("Fant ikke aktøren for arkivreferansen: $arkivreferanse")
            throw FantIkkeAktørException(null)
        }
        val inntektsmelding = mapInntektsmelding(arkivreferanse, aktorid, journalpostId, inntektsmeldingFraSimba)
        val dto = inntektsmeldingService.lagreBehandling(inntektsmelding, aktorid)

        utsattOppgaveService.opprett(
            UtsattOppgaveEntitet(
                fnr = inntektsmelding.fnr,
                aktørId = dto.aktorId,
                journalpostId = inntektsmelding.journalpostId,
                arkivreferanse = inntektsmelding.arkivRefereranse,
                inntektsmeldingId = dto.uuid,
                tilstand = Tilstand.Utsatt,
                timeout = LocalDateTime.now().plusHours(OPPRETT_OPPGAVE_FORSINKELSE),
                gosysOppgaveId = null,
                oppdatert = null,
                speil = false,
                utbetalingBruker = false
            )
        )

        val mappedInntektsmelding = mapInntektsmeldingKontrakt(
            inntektsmelding,
            aktorid,
            validerInntektsmelding(inntektsmelding),
            arkivreferanse,
            dto.uuid
        )

        inntektsmeldingAivenProducer.leggMottattInntektsmeldingPåTopics(mappedInntektsmelding)

        sikkerlogger.info("Publiserte inntektsmelding på topic: $inntektsmelding")
    }

    override suspend fun runReadynessCheck() {
        if (!ready) {
            throw IllegalStateException("Lytting på hendelser er ikke klar ennå")
        }
    }

    override suspend fun runLivenessCheck() {
        if (error) {
            throw IllegalStateException("Det har oppstått en feil og slutter å lytte på hendelser")
        }
    }
}
