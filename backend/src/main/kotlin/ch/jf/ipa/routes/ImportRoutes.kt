package ch.jf.ipa.routes

import ch.jf.ipa.service.PdfImportService
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.call
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.io.ByteArrayInputStream
import java.io.IOException
import io.ktor.utils.io.core.readBytes

fun Route.importRoutes(importService: PdfImportService) {
    route("/imports") {
        post("/ipa") {
            val multipart = call.receiveMultipart()
            var fileBytes: ByteArray? = null

            multipart.forEachPart { part ->
                when (part) {
                    is PartData.FileItem -> {
                        if (part.originalFileName?.endsWith(".pdf", ignoreCase = true) != false) {
                            fileBytes = part.provider().readBytes()
                        }
                        part.dispose()
                    }
                    else -> part.dispose()
                }
            }

            val bytes = fileBytes
            if (bytes == null || bytes.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "Keine PDF-Datei hochgeladen.")
                return@post
            }

            try {
                val result = importService.importIpa(ByteArrayInputStream(bytes))
                call.respond(HttpStatusCode.OK, result)
            } catch (ex: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    ex.message ?: "PDF konnte nicht verarbeitet werden.",
                )
            } catch (ex: IOException) {
                call.respond(HttpStatusCode.BadRequest, "PDF konnte nicht gelesen werden.")
            }
        }
    }
}
