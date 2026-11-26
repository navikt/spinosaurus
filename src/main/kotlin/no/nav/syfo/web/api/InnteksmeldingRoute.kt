package no.nav.syfo.web.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import no.nav.helsearbeidsgiver.utils.log.logger
import no.nav.helsearbeidsgiver.utils.log.sikkerLogger
import no.nav.syfo.mapping.mapInntektsmeldingKontrakt
import no.nav.syfo.service.InntektsmeldingService
import no.nav.syfo.util.InntektsmeldingJsonMapper
import no.nav.syfo.util.validerInntektsmelding
import java.util.UUID

fun Route.syfoinntektsmelding(inntektsmeldingService: InntektsmeldingService) {
    get("/{inntektsmeldingId}") {
        val inntektsmeldingId =
            call.parameters["inntektsmeldingId"]
                ?.runCatching(UUID::fromString)
                ?.getOrNull()
                ?: throw IllegalArgumentException("Forventet inntektsmeldingId som path parameter")

        logger().debug("Fikk request om å hente inntektsmelding med id: $inntektsmeldingId")

        try {
            val dto = inntektsmeldingService.findById(inntektsmeldingId)
            if (dto != null) {
                val inntektsmelding = InntektsmeldingJsonMapper.fromJson(dto.data, dto.fnr)

                val mappedInntektsmelding =
                    mapInntektsmeldingKontrakt(
                        inntektsmelding,
                        dto.aktorId,
                        validerInntektsmelding(inntektsmelding),
                        inntektsmelding.arkivRefereranse,
                        dto.uuid,
                    )

                logger().debug("Henter inntektsmelding med arkivreferanse: ${inntektsmelding.arkivRefereranse}")

                call.respond(HttpStatusCode.OK, mappedInntektsmelding)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("code" to HttpStatusCode.NotFound.value, "message" to "Inntektsmelding ikke funnet"),
                )
                return@get
            }
        } catch (e: Exception) {
            logger().error("Feil ved henting av inntektsmelding id $inntektsmeldingId (se securelogs for mer detaljer)")
            sikkerLogger().error("Fikk feil for inntektsmelding id $inntektsmeldingId - ${e.message}", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("code" to HttpStatusCode.InternalServerError.value, "message" to "Feil ved henting av inntektsmelding"),
            )
        }
    }
}

fun Route.refusjonskrav(inntektsmeldingService: InntektsmeldingService) {
    post("/refusjonskrav") {
        try {
            val request = call.receive<RefusjonskravRequest>()
            call.respond(HttpStatusCode.OK, inntektsmeldingService.hentRefusjonskrav(request.inntektsmeldingIder))
        } catch (_: BadRequestException) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("code" to HttpStatusCode.BadRequest.value, "message" to "Ugyldig request. Forventet liste med inntektsmelding-UUIDer"),
            )
        } catch (e: Exception) {
            logger().error("Feil ved henting av refusjonskrav (se securelogs for mer detaljer)")
            sikkerLogger().error("Feil ved henting av refusjonskrav - ${e.message}", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("code" to HttpStatusCode.InternalServerError.value, "message" to "Feil ved henting av refusjonskrav"),
            )
        }
    }
}
