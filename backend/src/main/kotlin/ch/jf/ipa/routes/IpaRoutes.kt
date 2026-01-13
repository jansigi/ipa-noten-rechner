package ch.jf.ipa.routes

import ch.jf.ipa.service.IpaService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import java.util.UUID

fun Route.ipaRoutes(ipaService: IpaService) {
    route("/ipa") {
        get("{personId}") {
            val personId =
                call.parameters["personId"]?.toUUIDOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "Invalid person id")

            val dataset = ipaService.getDatasetForPerson(personId)
            if (dataset == null) {
                call.respond(HttpStatusCode.NotFound, "Keine IPA für diese Person importiert.")
            } else {
                call.respond(dataset)
            }
        }

        get {
            val dataset = ipaService.getLatestDataset()
            if (dataset == null) {
                call.respond(HttpStatusCode.NotFound, "Keine IPA importiert.")
            } else {
                call.respond(dataset)
            }
        }
    }
}

private fun String.toUUIDOrNull(): UUID? = runCatching { UUID.fromString(this) }.getOrNull()
