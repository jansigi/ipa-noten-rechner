package ch.jf.ipa.routes

import ch.jf.ipa.service.MetadataService
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.metadataRoutes(metadataService: MetadataService) {
    route("/metadata") {
        get {
            val metadata = metadataService.getMetadata()
            call.respond(metadata)
        }
    }
}
