package ch.jf.ipa.routes

import ch.jf.ipa.dto.CriterionDto
import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.dto.PersonResultsDto
import ch.jf.ipa.dto.RequirementDto
import ch.jf.ipa.module
import ch.jf.ipa.repository.IpaRepository
import ch.jf.ipa.repository.IpaRepositoryImpl
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.testApplication
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ResultsRoutesTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `results endpoint returns grades for person`() = testApplication {
        environment {
            config = MapApplicationConfig().apply {
                put("ktor.database.url", "jdbc:h2:mem:results-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                put("ktor.database.driver", "org.h2.Driver")
                put("ktor.database.user", "sa")
                put("ktor.database.password", "")
            }
        }

        application {
            module()
        }

        val personResponse = client.post("/persons") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "firstName": "Grace",
                  "lastName": "Hopper",
                  "topic": "Compilers",
                  "submissionDate": "2025-05-01"
                }
                """.trimIndent(),
            )
        }
        assertEquals(HttpStatusCode.Created, personResponse.status)
        val createdPersonId = json.parseToJsonElement(personResponse.bodyAsText()).jsonObject["id"]!!.jsonPrimitive.content

        val ipaRepository: IpaRepository = IpaRepositoryImpl()
        runBlocking {
            ipaRepository.createForPerson(
                UUID.fromString(createdPersonId),
                IpaDatasetDto(
                    ipaName = "Test IPA",
                    topic = "Compilers",
                    criteria = listOf(
                        CriterionDto(
                            id = "A01",
                            title = "Criterion A01",
                            question = "Question A01",
                            requirements = listOf(
                                RequirementDto("A01-1", "Desc", "BF", 1),
                                RequirementDto("A01-2", "Desc", "BF", 2),
                                RequirementDto("A01-3", "Desc", "BF", 3),
                                RequirementDto("A01-4", "Desc", "BF", 4),
                            ),
                        ),
                    ),
                ),
            )
        }

        val progressResponse = client.post("/progress/$createdPersonId") {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                  "criterionId": "A01",
                  "checkedRequirements": ["A01-1", "A01-2", "A01-3", "INVALID"],
                  "note": "Good progress"
                }
                """.trimIndent(),
            )
        }
        assertEquals(HttpStatusCode.OK, progressResponse.status)

        val resultsResponse = client.get("/results/$createdPersonId")
        assertEquals(HttpStatusCode.OK, resultsResponse.status)

        val personResults = json.decodeFromString(PersonResultsDto.serializer(), resultsResponse.bodyAsText())
        val criterionResult = personResults.results.first { it.criterionId == "A01" }

        assertEquals(4, criterionResult.totalCount)
        assertEquals(3, criterionResult.fulfilledCount)
        assertEquals(2, criterionResult.gradeLevel)
        assertEquals(listOf("A01-1", "A01-2", "A01-3"), criterionResult.checkedRequirements)
    }

    @Test
    fun `results endpoint returns 404 for unknown person`() = testApplication {
        environment {
            config = MapApplicationConfig().apply {
                put("ktor.database.url", "jdbc:h2:mem:results-test-missing;DB_CLOSE_DELAY=-1;MODE=PostgreSQL")
                put("ktor.database.driver", "org.h2.Driver")
                put("ktor.database.user", "sa")
                put("ktor.database.password", "")
            }
        }

        application {
            module()
        }

        val response = client.get("/results/${UUID.randomUUID()}")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
