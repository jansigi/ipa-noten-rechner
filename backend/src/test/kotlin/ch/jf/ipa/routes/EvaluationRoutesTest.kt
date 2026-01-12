package ch.jf.ipa.routes

import ch.jf.ipa.model.Person
import ch.jf.ipa.module
import ch.jf.ipa.repository.CriterionProgressRepository
import ch.jf.ipa.repository.CriterionProgressRepositoryImpl
import ch.jf.ipa.repository.PersonRepository
import ch.jf.ipa.repository.PersonRepositoryImpl
import io.ktor.client.statement.bodyAsText
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking

class EvaluationRoutesTest {
    @Test
    fun evaluateReturnsGrades() =
        testApplication {
            environment {
                config =
                    MapApplicationConfig().apply {
                        put("ktor.database.url", "jdbc:h2:mem:eval-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                        put("ktor.database.driver", "org.h2.Driver")
                        put("ktor.database.user", "sa")
                        put("ktor.database.password", "")
                    }
            }

            application { module() }

            val personRepository: PersonRepository = PersonRepositoryImpl()
            val progressRepository: CriterionProgressRepository = CriterionProgressRepositoryImpl()

            val personId = UUID.randomUUID()
            val person =
                Person(
                    id = personId,
                    firstName = "Test",
                    lastName = "User",
                    topic = "Eval",
                    submissionDate = LocalDate.now(),
                )

            // Create person in DB
            runBlocking { personRepository.create(person) }

            // No progress yet -> all grades should be 0
            val response = client.get("/evaluation/$personId")
            assertEquals(HttpStatusCode.OK, response.status)
            val body = response.bodyAsText()
            // Ensure some criteria are returned and grade 0 appears
            assert(body.contains("\"criterionId\""))
            assert(body.contains("\"grade\":0"))
        }
}
