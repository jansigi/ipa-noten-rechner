package ch.ergon.ipa

import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class EngineTest {
    @Test
    fun applicationStarts() = testApplication {
        environment {
            config = MapApplicationConfig()
        }

        application {
            module()
        }

        val response = client.get("/persons")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}

