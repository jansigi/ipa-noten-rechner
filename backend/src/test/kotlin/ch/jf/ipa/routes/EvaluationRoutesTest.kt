package ch.jf.ipa.routes

import ch.jf.ipa.dto.CriterionDto
import ch.jf.ipa.dto.EvaluatedCriterionDto
import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.dto.RequirementDto
import ch.jf.ipa.model.Person
import ch.jf.ipa.module
import ch.jf.ipa.repository.IpaRepository
import ch.jf.ipa.repository.IpaRepositoryImpl
import ch.jf.ipa.repository.PersonRepository
import ch.jf.ipa.repository.PersonRepositoryImpl
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class EvaluationRoutesTest {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun evaluateReturnsGrades() = testApplication {
        environment {
            config = MapApplicationConfig().apply {
                put("ktor.database.url", "jdbc:h2:mem:eval-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                put("ktor.database.driver", "org.h2.Driver")
                put("ktor.database.user", "sa")
                put("ktor.database.password", "")
            }
        }

        application { module() }

        // Ensure the application (and DatabaseFactory) is initialized.
        client.get("/persons")

        val personRepository: PersonRepository = PersonRepositoryImpl()
        val ipaRepository: IpaRepository = IpaRepositoryImpl()

        val personId = UUID.randomUUID()
        runBlocking {
            personRepository.create(
                Person(
                    id = personId,
                    firstName = "Test",
                    lastName = "User",
                    topic = "Eval",
                    submissionDate = LocalDate.now(),
                ),
            )
            ipaRepository.createForPerson(
                personId,
                IpaDatasetDto(
                    ipaName = "Test IPA",
                    topic = "Eval",
                    criteria = listOf(
                        CriterionDto(
                            id = "A01",
                            title = "Criterion A01",
                            question = "Question A01",
                            requirements = listOf(
                                RequirementDto("A01-1", "Desc", "BF", 1),
                                RequirementDto("A01-2", "Desc", "BF", 2),
                            ),
                        ),
                    ),
                ),
            )
        }

        val response = client.get("/evaluation/$personId")
        assertEquals(HttpStatusCode.OK, response.status)

        val evaluated = json.decodeFromString(
            ListSerializer(EvaluatedCriterionDto.serializer()),
            response.bodyAsText(),
        )

        assertEquals(1, evaluated.size)
        assertEquals("A01", evaluated.first().criterionId)
        assertEquals(0, evaluated.first().grade)
    }
}
