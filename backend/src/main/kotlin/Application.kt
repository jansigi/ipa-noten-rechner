package ch.jf

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json


fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(
            Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
        )
    }

    install(CallLogging)

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("MyCustomHeader")
        anyHost() // Limit in production
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val status = HttpStatusCode.InternalServerError
            call.respond(
                status,
                mapOf(
                    "error" to status.description,
                    "message" to (cause.message ?: "Unexpected error"),
                    "status" to status.value
                )
            )
        }
    }

    routing {
        get("/health") {
            call.respond(mapOf("status" to "ok"))
        }
    }
}
