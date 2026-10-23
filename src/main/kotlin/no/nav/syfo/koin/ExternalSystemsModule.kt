package no.nav.syfo.koin

import io.ktor.server.config.ApplicationConfig
import no.nav.auth.AuthClient
import no.nav.auth.IdentityProvider
import no.nav.auth.fetchToken
import no.nav.helsearbeidsgiver.oppgave.OppgaveClient
import no.nav.helsearbeidsgiver.pdl.Behandlingsgrunnlag
import no.nav.helsearbeidsgiver.pdl.PdlClient
import no.nav.helsearbeidsgiver.utils.cache.LocalCache
import no.nav.syfo.client.norg.Norg2Client
import no.nav.syfo.util.getString
import org.koin.core.module.Module
import org.koin.dsl.bind
import kotlin.time.Duration.Companion.days

fun Module.externalSystemClients(config: ApplicationConfig) {
    single {
        AuthClient(
            httpClient = get(),
            tokenEndpoint = config.getString("auth.token_endpoint"),
        )
    }

    single {
        val azureClient: AuthClient = get()
        PdlClient(
            url = config.getString("pdl_url"),
            behandlingsgrunnlag = Behandlingsgrunnlag.INNTEKTSMELDING,
            cacheConfig = LocalCache.Config(1.days, 30_000),
            getAccessToken = azureClient.fetchToken(IdentityProvider.AZURE_AD, config.getString("auth.pdlscope")),
        )
    } bind PdlClient::class
    single {
        val azureClient: AuthClient = get()
        OppgaveClient(
            url = config.getString("oppgavebehandling_url"),
            getToken = azureClient.fetchToken(IdentityProvider.AZURE_AD, config.getString("auth.oppgavescope")),
        )
    }
    single {
        Norg2Client(
            url = config.getString("norg2_url"),
            httpClient = get(),
        )
    }
}
