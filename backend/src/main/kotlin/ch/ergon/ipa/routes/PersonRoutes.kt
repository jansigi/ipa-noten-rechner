package ch.ergon.ipa.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.personRoutes() {
    get("/persons") {
        call.respond(emptyList<Map<String, String>>())
    }

    post("/persons") {
        call.respond(HttpStatusCode.NotImplemented)
    }
}

