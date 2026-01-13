package ch.jf.ipa.routes

import ch.jf.ipa.service.CriteriaProvider
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.criteriaRoutes(criteriaProvider: CriteriaProvider) {
    route("/criteria") {
        get {
            val criteria = criteriaProvider.getAllCriteria()
            call.respond(criteria)
        }
    }
}
