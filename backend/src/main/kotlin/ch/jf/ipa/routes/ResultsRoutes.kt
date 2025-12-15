package ch.jf.ipa.routes

import ch.jf.ipa.dto.CriterionResultDto
import ch.jf.ipa.repository.PersonRepository
import ch.jf.ipa.service.GradingService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

fun Route.resultsRoutes(personRepository: PersonRepository, gradingService: GradingService) {
    route("/results") {
        get("{personId}") {
            val personId = call.parameters["personId"]?.toUUIDOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Invalid person id",
            )

            val personExists = personRepository.getById(personId) != null
            if (!personExists) {
                call.respond(HttpStatusCode.NotFound, "Person not found")
                return@get
            }

            val results = gradingService.calculateResultsForPerson(personId)
            call.respond(results)
        }

        get("{personId}/{criterionId}") {
            val personId = call.parameters["personId"]?.toUUIDOrNull() ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                "Invalid person id",
            )
            val criterionId = call.parameters["criterionId"]
            if (criterionId.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest, "Invalid criterion id")
                return@get
            }

            val personExists = personRepository.getById(personId) != null
            if (!personExists) {
                call.respond(HttpStatusCode.NotFound, "Person not found")
                return@get
            }

            val results = gradingService.calculateResultsForPerson(personId)
            val criterionResult = results.results.firstOrNull { it.criterionId == criterionId }
            if (criterionResult == null) {
                call.respond(HttpStatusCode.NotFound, "Criterion not found")
                return@get
            }

            call.respond(criterionResult)
        }
    }
}

private fun String.toUUIDOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()

