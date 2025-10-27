package no.nav.syfo.integration.kafka.journalpost

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.hag.utils.bakgrunnsjobb.Bakgrunnsjobb
import no.nav.hag.utils.bakgrunnsjobb.BakgrunnsjobbRepository
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger
import no.nav.syfo.kafkamottak.InngaaendeJournalpostDTO
import no.nav.syfo.prosesser.JoarkInntektsmeldingHendelseProsessor
import no.nav.syfo.util.LivenessComponent
import no.nav.syfo.util.ReadynessComponent
import org.apache.avro.generic.GenericRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import java.time.Duration.ofMillis
import java.time.LocalDateTime

enum class JournalpostStatus {
    Ny,
    IkkeInntektsmelding,
    FeilHendelseType,
}

class JournalpostHendelseConsumer(
    props: Map<String, Any>,
    topicName: String,
    private val bakgrunnsjobbRepo: BakgrunnsjobbRepository,
    private val om: ObjectMapper,
) : ReadynessComponent,
    LivenessComponent {
    private val logger = logger()
    private val sikkerlogger = sikkerLogger()
    private val consumer: KafkaConsumer<String, GenericRecord> = KafkaConsumer(props)
    private var ready = false
    private var error = false

    init {
        logger.info("Lytter på topic $topicName")
        consumer.subscribe(listOf(topicName))
    }

    fun setIsReady(ready: Boolean) {
        this.ready = ready
    }

    fun setIsError(isError: Boolean) {
        this.error = isError
    }

    fun start() {
        logger.info("Starter...")
        consumer.use {
            setIsReady(true)
            while (!error) {
                it.poll(ofMillis(1000)).forEach { record ->
                    try {
                        val partition = record.partition()
                        val topicPartition = TopicPartition(record.topic(), partition)
                        logger.info("Journalpost Record offset: ${record.offset()}, partition: $partition")
                        val nyttOffset = OffsetAndMetadata(record.offset() + 1)
                        // TODO : dele opp consumer og processHendelse i forskjellige klasser, kan gjøre test av kafkaConsumer-logikk enklere
                        processHendelse(mapJournalpostHendelse(record.value()))
                        it.commitSync(mapOf(topicPartition to nyttOffset))
                    } catch (e: Throwable) {
                        "Klarte ikke behandle hendelse. Stopper lytting!".also {
                            logger.error(it)
                            sikkerlogger.error(it, e)
                        }
                        setIsError(true)
                    }
                }
            }
        }
    }

    fun processHendelse(journalpostDTO: InngaaendeJournalpostDTO) {
        when (findStatus(journalpostDTO)) {
            JournalpostStatus.Ny -> lagreBakgrunnsjobb(journalpostDTO)
            JournalpostStatus.IkkeInntektsmelding ->
                logger.debug(
                    "Ignorerte journalposthendelse ${journalpostDTO.hendelsesId}. Kanal: ${journalpostDTO.mottaksKanal} Tema: ${journalpostDTO.temaNytt} Status: ${journalpostDTO.journalpostStatus}",
                )

            JournalpostStatus.FeilHendelseType ->
                logger.debug(
                    "Ingorerte JournalpostHendelse ${journalpostDTO.hendelsesId} av type ${journalpostDTO.hendelsesType} med referanse: ${journalpostDTO.kanalReferanseId}",
                )
        }
    }

    fun findStatus(journalpostDTO: InngaaendeJournalpostDTO): JournalpostStatus {
        if (isInntektsmelding(journalpostDTO)) {
            if (journalpostDTO.hendelsesType != "JournalpostMottatt") {
                return JournalpostStatus.FeilHendelseType
            }
            return JournalpostStatus.Ny
        }
        return JournalpostStatus.IkkeInntektsmelding
    }

    private fun lagreBakgrunnsjobb(hendelse: InngaaendeJournalpostDTO) {
        logger.debug("Lagrer inntektsmelding ${hendelse.kanalReferanseId} for hendelse ${hendelse.hendelsesId}")
        bakgrunnsjobbRepo.save(
            Bakgrunnsjobb(
                type = JoarkInntektsmeldingHendelseProsessor.JOB_TYPE,
                kjoeretid = LocalDateTime.now(),
                maksAntallForsoek = 10,
                data = om.writeValueAsString(hendelse),
            ),
        )
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

fun isInntektsmelding(hendelse: InngaaendeJournalpostDTO): Boolean = hendelse.temaNytt == "SYK" && hendelse.mottaksKanal == "ALTINN" && hendelse.journalpostStatus == "MOTTATT"
