package ch.jf.ipa.routes

import ch.jf.ipa.dto.CriterionProgressRequest
import ch.jf.ipa.dto.toDomain
import ch.jf.ipa.dto.toResponseDto
import ch.jf.ipa.repository.CriterionProgressRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID

fun Route.progressRoutes(progressRepository: CriterionProgressRepository) {
    route("/progress") {
        get("{personId}") {
            val personId =
                call.parameters["personId"]?.toUUIDOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, "Invalid person id")
                        return@get
                    }

            val progress = progressRepository.getByPersonId(personId).map { it.toResponseDto() }
            call.respond(progress)
        }

        post("{personId}") {
            val personId =
                call.parameters["personId"]?.toUUIDOrNull()
                    ?: run {
                        call.respond(HttpStatusCode.BadRequest, "Invalid person id")
                        return@post
                    }

            val request = call.receive<CriterionProgressRequest>()
            val domain = request.toDomain(personId)
            val saved = progressRepository.createOrUpdate(domain)
            call.respond(HttpStatusCode.OK, saved.toResponseDto())
        }
    }
}

private fun String.toUUIDOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
