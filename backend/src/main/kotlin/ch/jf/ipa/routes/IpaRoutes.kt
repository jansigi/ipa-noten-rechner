package ch.jf.ipa.routes

import ch.jf.ipa.service.IpaService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.ipaRoutes(ipaService: IpaService) {
    route("/ipa") {
        get {
            val dataset = ipaService.getActiveDataset()
            if (dataset == null) {
                call.respond(HttpStatusCode.NotFound, "Keine IPA importiert.")
            } else {
                call.respond(dataset)
            }
        }
    }
}

