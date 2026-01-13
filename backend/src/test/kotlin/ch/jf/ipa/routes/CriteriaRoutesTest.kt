package ch.jf.ipa.routes

import ch.jf.ipa.module
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.client.statement.bodyAsText
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CriteriaRoutesTest {
    @Test
    fun getCriteriaReturnsList() =
        testApplication {
            environment {
                config =
                    MapApplicationConfig().apply {
                        put("ktor.database.url", "jdbc:h2:mem:criteria-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                        put("ktor.database.driver", "org.h2.Driver")
                        put("ktor.database.user", "sa")
                        put("ktor.database.password", "")
                    }
            }

            application { module() }

            val response = client.get("/criteria")
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            assertTrue(body.contains("\"id\":"))
        }
}
