package ch.jf.ipa.routes

import ch.jf.ipa.dto.CreatePersonRequest
import ch.jf.ipa.dto.toPerson
import ch.jf.ipa.dto.toResponseDto
import ch.jf.ipa.repository.PersonRepository
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.personRoutes(personRepository: PersonRepository) {
    route("/persons") {
        get {
            val persons = personRepository.getAll().map { it.toResponseDto() }
            call.respond(persons)
        }

        post {
            val request = call.receive<CreatePersonRequest>()
            val person =
                runCatching { request.toPerson() }
                    .getOrElse {
                        call.respond(HttpStatusCode.BadRequest, "Invalid person payload")
                        return@post
                    }

            val created = personRepository.create(person)
            call.respond(HttpStatusCode.Created, created.toResponseDto())
        }
    }
}
