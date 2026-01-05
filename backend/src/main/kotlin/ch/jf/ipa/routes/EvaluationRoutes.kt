package ch.jf.ipa.routes

import ch.jf.ipa.service.EvaluationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

fun Route.evaluationRoutes(evaluationService: EvaluationService) {
    route("/evaluation") {
        get("{personId}") {
            val personId = call.parameters["personId"]?.toUUIDOrNull()
                ?: run {
                    call.respond(HttpStatusCode.BadRequest, "Invalid person id")
                    return@get
                }

            val result = evaluationService.evaluate(personId)
            call.respond(result)
        }
    }
}

private fun String.toUUIDOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()

