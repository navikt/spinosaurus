package no.nav.syfo.web

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.ContentType
import io.ktor.serialization.jackson.JacksonConverter
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.authentication
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.security.token.support.v3.IssuerConfig
import no.nav.security.token.support.v3.TokenSupportConfig
import no.nav.security.token.support.v3.tokenValidationSupport
import no.nav.syfo.Env
import no.nav.syfo.service.InntektsmeldingService
import no.nav.syfo.util.customObjectMapper
import no.nav.syfo.web.api.refusjonskrav
import no.nav.syfo.web.api.syfoinntektsmelding

private val objectMapper = customObjectMapper()

fun Application.inntektsmeldingModule(inntektsmeldingService: InntektsmeldingService) {
    customAuthentication()

    install(ContentNegotiation) {
        register(ContentType.Application.Json, JacksonConverter(objectMapper))
        // Kan denne fjernes? JavaTimeModule er allerede registrert i customObjectMapper
        jackson {
            registerModule(JavaTimeModule())
        }
    }
    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        route("/api/v1") {
            authenticate {
                route("/inntektsmelding") {
                    syfoinntektsmelding(inntektsmeldingService)
                    refusjonskrav(inntektsmeldingService)
                }
            }
        }
    }
}

private fun Application.customAuthentication() {
    val config =
        TokenSupportConfig(
            IssuerConfig(
                name = Auth.ISSUER,
                discoveryUrl = Env.Auth.discoveryUrl,
                acceptedAudience = Env.Auth.acceptedAudience,
            ),
        )

    val allowList =
        Env.Auth.preauthorizedApps
            .let { objectMapper.readTree(it) }
            .map { it["clientId"].asText() }

    authentication {
        tokenValidationSupport(
            config = config,
            additionalValidation = {
                val clientId =
                    it
                        .getClaims(Auth.ISSUER)
                        .getStringClaim(Auth.CLAIM_AZP)
                clientId in allowList
            },
        )
    }
}

private object Auth {
    const val ISSUER = "hagproxy"
    const val CLAIM_AZP = "azp"
}
