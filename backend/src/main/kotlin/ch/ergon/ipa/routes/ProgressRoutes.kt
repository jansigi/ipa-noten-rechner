package ch.ergon.ipa.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post

fun Route.progressRoutes() {
    get("/progress/{personId}") {
        call.respond(emptyList<Map<String, String>>())
    }

    post("/progress/{personId}") {
        call.respond(HttpStatusCode.NotImplemented)
    }
}

