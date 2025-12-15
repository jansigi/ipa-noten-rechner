package ch.ergon.ipa

import ch.ergon.ipa.routes.personRoutes
import ch.ergon.ipa.routes.progressRoutes
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.routing
import org.koin.core.module.Module
import org.koin.ktor.plugin.Koin

fun main(args: Array<String>) = EngineMain.main(args)

@Suppress("unused")
fun Application.module() {
    install(CallLogging)

    install(ContentNegotiation) {
        json()
    }

    install(Koin) {
        modules(emptyList<Module>())
    }

    routing {
        personRoutes()
        progressRoutes()
    }
}

