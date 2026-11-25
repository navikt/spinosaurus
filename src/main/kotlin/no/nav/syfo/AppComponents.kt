package no.nav.syfo

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import no.nav.auth.AuthClient
import no.nav.auth.IdentityProvider
import no.nav.auth.fetchToken
import no.nav.hag.utils.bakgrunnsjobb.BakgrunnsjobbService
import no.nav.hag.utils.bakgrunnsjobb.PostgresBakgrunnsjobbRepository
import no.nav.helsearbeidsgiver.oppgave.OppgaveClient
import no.nav.helsearbeidsgiver.pdl.Behandlingsgrunnlag
import no.nav.helsearbeidsgiver.pdl.PdlClient
import no.nav.helsearbeidsgiver.utils.cache.LocalCache
import no.nav.syfo.client.norg.Norg2Client
import no.nav.syfo.client.oppgave.OppgaveService
import no.nav.syfo.integration.kafka.UtsattOppgaveConsumer
import no.nav.syfo.integration.kafka.commonAivenProps
import no.nav.syfo.integration.kafka.inntektsmeldingAivenProps
import no.nav.syfo.integration.kafka.utsattOppgaveAivenProps
import no.nav.syfo.producer.InntektsmeldingAivenProducer
import no.nav.syfo.prosesser.FinnAlleUtgaandeOppgaverProcessor
import no.nav.syfo.prosesser.LeaderElectionBakgrunnsjobbProcessor
import no.nav.syfo.repository.InntektsmeldingRepository
import no.nav.syfo.repository.UtsattOppgaveRepositoryImp
import no.nav.syfo.service.BehandlendeEnhetService
import no.nav.syfo.service.InntektsmeldingService
import no.nav.syfo.simba.InntektsmeldingConsumer
import no.nav.syfo.util.LeaderElectionManager
import no.nav.syfo.util.createHttpClient
import no.nav.syfo.utsattoppgave.FeiletUtsattOppgaveMeldingProsessor
import no.nav.syfo.utsattoppgave.UtsattOppgaveDAO
import no.nav.syfo.utsattoppgave.UtsattOppgaveService
import kotlin.time.Duration.Companion.days

val hikariDataSource =
    HikariDataSource(
        HikariConfig().apply {
            jdbcUrl = Env.Database.url
            username = Env.Database.username
            password = Env.Database.password
            minimumIdle = 1
            maximumPoolSize = 5
            maxLifetime = 30001
            connectionTimeout = 2000
            idleTimeout = 10001
            driverClassName = "org.postgresql.Driver"
        },
    )

private object Client {
    private val httpClient =
        createHttpClient(maxRetries = 3)

    private val authClient =
        AuthClient(
            tokenEndpoint = Env.Client.naisTokenEndpoint,
            httpClient = httpClient,
        )

    val norg2 =
        Norg2Client(
            url = Env.Client.urlNorg2,
            httpClient = httpClient,
        )

    val oppgave =
        OppgaveClient(
            url = Env.Client.urlOppgave,
            getToken = authClient.fetchToken(IdentityProvider.AZURE_AD, Env.Client.scopeOppgave),
        )

    val pdl =
        PdlClient(
            url = Env.Client.urlPdl,
            behandlingsgrunnlag = Behandlingsgrunnlag.INNTEKTSMELDING,
            cacheConfig = LocalCache.Config(1.days, 30_000),
            getAccessToken = authClient.fetchToken(IdentityProvider.AZURE_AD, Env.Client.scopePdl),
        )

    val leaderElectionManager =
        LeaderElectionManager(
            url = Env.Client.urlElector,
            httpClient = httpClient,
        )
}

object Inntektsmelding {
    private val aivenProducer = InntektsmeldingAivenProducer(commonAivenProps())
    private val repository = InntektsmeldingRepository(hikariDataSource)

    val service = InntektsmeldingService(repository)
    val consumer =
        InntektsmeldingConsumer(
            inntektsmeldingAivenProps(),
            Env.Kafka.topicInntektsmelding,
            service,
            aivenProducer,
            UtsattOppgave.service,
            Client.pdl,
        )
}

object UtsattOppgave {
    // repo
    private val bakgrunnsjobbRepository = PostgresBakgrunnsjobbRepository(hikariDataSource)
    private val utsattOppgaveRepository = UtsattOppgaveRepositoryImp(hikariDataSource)
    private val utsattOppgaveDao = UtsattOppgaveDAO(utsattOppgaveRepository)

    // service
    private val bakgrunnsjobbService = BakgrunnsjobbService(bakgrunnsjobbRepository, bakgrunnsvarsler = MetrikkVarsler())
    private val behandlendeEnhetService = BehandlendeEnhetService(Client.pdl, Client.norg2)
    private val oppgaveService = OppgaveService(Client.oppgave)

    val service =
        UtsattOppgaveService(
            utsattOppgaveDao,
            oppgaveService,
            behandlendeEnhetService,
            Inntektsmelding.service,
        )
    val consumer =
        UtsattOppgaveConsumer(
            utsattOppgaveAivenProps(),
            Env.Kafka.topicUtsattOppgave,
            service,
            bakgrunnsjobbRepository,
        )

    private val feiletUtsattOppgaveMeldingProsessor = FeiletUtsattOppgaveMeldingProsessor(service)
    private val finnAlleUtgaandeOppgaverProcessor =
        FinnAlleUtgaandeOppgaverProcessor(
            utsattOppgaveDao,
            oppgaveService,
            behandlendeEnhetService,
            Inntektsmelding.service,
        )

    val leaderElectionBakgrunnsjobbProcessor =
        LeaderElectionBakgrunnsjobbProcessor(
            Client.leaderElectionManager,
            bakgrunnsjobbService,
            finnAlleUtgaandeOppgaverProcessor,
            feiletUtsattOppgaveMeldingProsessor,
        )
}
