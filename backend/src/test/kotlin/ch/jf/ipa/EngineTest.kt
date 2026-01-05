package ch.jf.ipa

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
            config = MapApplicationConfig().apply {
                put("ktor.database.url", "jdbc:h2:mem:engine-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                put("ktor.database.driver", "org.h2.Driver")
                put("ktor.database.user", "sa")
                put("ktor.database.password", "")
            }
        }

        application {
            module()
        }

        val response = client.get("/persons")
        assertEquals(HttpStatusCode.OK, response.status)
    }
}

