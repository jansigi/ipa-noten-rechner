package ch.jf.ipa

import ch.jf.ipa.config.DatabaseFactory
import ch.jf.ipa.config.repositoryModule
import ch.jf.ipa.config.serviceModule
import ch.jf.ipa.repository.CriterionProgressRepository
import ch.jf.ipa.repository.PersonRepository
import ch.jf.ipa.routes.personRoutes
import ch.jf.ipa.routes.progressRoutes
import ch.jf.ipa.routes.resultsRoutes
import ch.jf.ipa.service.GradingService
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    DatabaseFactory.init(environment)

    install(CallLogging)

    install(ContentNegotiation) {
        json()
    }

    install(Koin) {
        modules(repositoryModule, serviceModule)
    }

    val personRepository by inject<PersonRepository>()
    val progressRepository by inject<CriterionProgressRepository>()
    val gradingService by inject<GradingService>()

    routing {
        personRoutes(personRepository)
        progressRoutes(progressRepository)
        resultsRoutes(personRepository, gradingService)
    }
}

