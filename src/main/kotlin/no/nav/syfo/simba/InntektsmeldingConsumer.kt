package no.nav.syfo.simba

import kotlinx.coroutines.runBlocking
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.Inntektsmelding
import no.nav.helsearbeidsgiver.domene.inntektsmelding.v1.JournalfoertInntektsmelding
import no.nav.helsearbeidsgiver.pdl.PdlClient
import no.nav.helsearbeidsgiver.utils.json.fromJson
import no.nav.helsearbeidsgiver.utils.json.parseJson
import no.nav.helsearbeidsgiver.utils.json.toPretty
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger
import no.nav.syfo.behandling.FantIkkeAktørException
import no.nav.syfo.dto.Tilstand
import no.nav.syfo.dto.UtsattOppgaveEntitet
import no.nav.syfo.mapping.mapInntektsmeldingKontrakt
import no.nav.syfo.producer.InntektsmeldingAivenProducer
import no.nav.syfo.service.InntektsmeldingService
import no.nav.syfo.util.InntektsmeldingJsonMapper
import no.nav.syfo.util.LivenessComponent
import no.nav.syfo.util.ReadynessComponent
import no.nav.syfo.utsattoppgave.UtsattOppgaveService
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import java.time.Duration
import java.time.LocalDateTime

const val OPPRETT_OPPGAVE_FORSINKELSE = 48L

class InntektsmeldingConsumer(
    props: Map<String, Any>,
    topicName: String,
    private val pdlClient: PdlClient,
    private val inntektsmeldingService: InntektsmeldingService,
    private val utsattOppgaveService: UtsattOppgaveService,
    private val inntektsmeldingAivenProducer: InntektsmeldingAivenProducer,
) : ReadynessComponent,
    LivenessComponent {
    private val consumer = KafkaConsumer<String, String>(props)
    private var ready = false
    private var error = false
    private val logger = logger()
    private val sikkerlogger = sikkerLogger()

    init {
        logger.info("Lytter på topic $topicName")
        consumer.subscribe(listOf(topicName))
    }

    private fun setIsReady() {
        this.ready = true
    }

    private fun setIsError() {
        this.error = true
    }

    fun start() {
        sikkerlogger.info("Starter InntektsmeldingConsumer...")
        consumer.use {
            setIsReady()
            while (!error) {
                it
                    .poll(Duration.ofMillis(100))
                    .forEach { record ->
                        try {
                            behandleRecord(record)
                            it.commitSync()
                        } catch (e: Throwable) {
                            sikkerlogger.error("InntektsmeldingConsumer: Klarte ikke behandle: ${record.value()}", e)
                            logger.error("InntektsmeldingConsumer: Klarte ikke behandle hendelse. Stopper lytting!")
                            setIsError()
                        }
                    }
            }
        }
    }

    private fun behandleRecord(record: ConsumerRecord<String, String>) {
        val key = record.key()
        val json = record.value().parseJson()
        sikkerlogger.info("InntektsmeldingConsumer: Mottar record med key: $key fra Simba.\n${json.toPretty()}")
        logger.info("InntektsmeldingConsumer: Mottar record med key: $key fra Simba}")
        val meldingFraSimba = json.fromJson(JournalfoertInntektsmelding.serializer())

        val im = inntektsmeldingService.findById(meldingFraSimba.inntektsmelding.id)
        if (im != null) {
            logger.info("InntektsmeldingConsumer: Behandler ikke ${meldingFraSimba.inntektsmelding.id}. Finnes allerede.")
        } else {
            behandle(meldingFraSimba.journalpostId, meldingFraSimba.inntektsmelding)
            logger.info("InntektsmeldingConsumer: Behandlet inntektsmelding med ID: ${meldingFraSimba.inntektsmelding.id}")
        }
    }

    fun behandle(
        journalpostId: String,
        inntektsmeldingFraSimba: Inntektsmelding,
    ) {
        val aktorId = hentAktoerIdFraPdl(inntektsmeldingFraSimba.sykmeldt.fnr.verdi)
        val arkivReferanse = "im_$journalpostId"
        val timeout =
            if (inntektsmeldingFraSimba.skalSendesTilSpleis()) {
                LocalDateTime.now().plusHours(OPPRETT_OPPGAVE_FORSINKELSE)
            } else {
                LocalDateTime.now() // opprett oppgave umiddelbart
            }

        val inntektsmelding = mapInntektsmelding(arkivReferanse, aktorId, inntektsmeldingFraSimba)

        inntektsmeldingService.lagreBehandling(
            inntektsmeldingFraSimba,
            aktorId,
            journalpostId,
            InntektsmeldingJsonMapper.toJson(inntektsmelding),
        )

        utsattOppgaveService.opprett(
            UtsattOppgaveEntitet(
                fnr = inntektsmeldingFraSimba.sykmeldt.fnr.verdi,
                aktørId = aktorId,
                journalpostId = journalpostId,
                arkivreferanse = arkivReferanse,
                inntektsmeldingId = inntektsmeldingFraSimba.id.toString(),
                tilstand = Tilstand.Utsatt,
                timeout = timeout,
                gosysOppgaveId = null,
                oppdatert = null,
                speil = false,
                utbetalingBruker = false,
            ),
        )

        val mappedInntektsmelding =
            mapInntektsmeldingKontrakt(
                inntektsmelding,
                aktorId,
                arkivReferanse,
                inntektsmeldingFraSimba.id,
            )

        inntektsmeldingAivenProducer.sendTilTopicForBruker(mappedInntektsmelding)

        if (inntektsmeldingFraSimba.skalSendesTilSpleis()) {
            inntektsmeldingAivenProducer.sendTilTopicForVedtaksloesning(mappedInntektsmelding)
        }
    }

    private fun hentAktoerIdFraPdl(identitetsnummer: String): String {
        val aktoerId =
            runBlocking {
                pdlClient.hentAktoerID(identitetsnummer)
            }

        return if (aktoerId == null) {
            logger.error("Fant ikke aktørId!")
            sikkerlogger.error("Fant ikke aktøren for: $identitetsnummer")
            throw FantIkkeAktørException(null)
        } else {
            aktoerId
        }
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
