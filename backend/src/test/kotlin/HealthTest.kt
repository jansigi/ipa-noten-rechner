package ch.jf

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthTest {
    @Test
    fun healthReturnsOk() = testApplication {
        application { module() }

        val response: HttpResponse = client.get("/health")
        assertEquals(HttpStatusCode.OK, response.status)

        val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
        assertEquals("ok", json["status"]?.jsonPrimitive?.content)
    }
}
