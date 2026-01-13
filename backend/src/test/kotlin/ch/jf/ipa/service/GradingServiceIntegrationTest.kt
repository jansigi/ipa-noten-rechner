package ch.jf.ipa.service

import ch.jf.ipa.dto.CriterionDto
import ch.jf.ipa.dto.RequirementDto
import ch.jf.ipa.model.CriterionProgress
import ch.jf.ipa.model.Person
import ch.jf.ipa.repository.CriterionProgressRepository
import ch.jf.ipa.repository.CriterionProgressRepositoryImpl
import ch.jf.ipa.repository.PersonRepository
import ch.jf.ipa.repository.PersonRepositoryImpl
import ch.jf.ipa.repository.RepositoryTestBase
import java.time.LocalDate
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class GradingServiceIntegrationTest : RepositoryTestBase() {

    private lateinit var personRepository: PersonRepository
    private lateinit var progressRepository: CriterionProgressRepository
    private lateinit var gradingService: GradingService
    private lateinit var personId: UUID

    @BeforeTest
    fun setup() {
        initDatabase()
        personRepository = PersonRepositoryImpl()
        progressRepository = CriterionProgressRepositoryImpl()
        gradingService = GradingService(
            criteriaProvider = FakePersonCriteriaProvider,
            progressRepository = progressRepository,
        )

        val person = Person(
            id = UUID.randomUUID(),
            firstName = "Alan",
            lastName = "Turing",
            topic = "Enigma",
            submissionDate = LocalDate.of(2025, 5, 1),
        )
        personId = person.id

        kotlinx.coroutines.test.runTest {
            personRepository.create(person)
            progressRepository.createOrUpdate(
                CriterionProgress(
                    id = UUID.randomUUID(),
                    personId = personId,
                    criterionId = "A01",
                    checkedRequirements = listOf("A01-1", "INVALID"),
                    note = "Needs work",
                ),
            )
        }
    }

    @Test
    fun `calculate results returns grades and filtered requirements`() = kotlinx.coroutines.test.runTest {
        val result = gradingService.calculateResultsForPerson(personId)

        assertEquals(personId, result.personId)
        assertEquals(2, result.results.size)

        val first = result.results.first { it.criterionId == "A01" }
        assertEquals(2, first.totalCount)
        assertEquals(1, first.fulfilledCount)
        assertEquals(1, first.gradeLevel)
        assertEquals(listOf("A01-1"), first.checkedRequirements)
        assertEquals("Needs work", first.note)
        assertEquals("Criterion A01", first.title)

        val second = result.results.first { it.criterionId == "A02" }
        assertEquals(3, second.totalCount)
        assertEquals(0, second.fulfilledCount)
        assertEquals(0, second.gradeLevel)
        assertEquals(emptyList(), second.checkedRequirements)
        assertNotNull(second.title)
    }

    private object FakePersonCriteriaProvider : PersonCriteriaProvider {
        private val criteria = listOf(
            CriterionDto(
                id = "A01",
                title = "Criterion A01",
                question = "Question A01",
                requirements = listOf(
                    RequirementDto("A01-1", "Desc", "BF", 1),
                    RequirementDto("A01-2", "Desc", "BF", 2),
                ),
            ),
            CriterionDto(
                id = "A02",
                title = "Criterion A02",
                question = "Question A02",
                requirements = listOf(
                    RequirementDto("A02-1", "Desc", "BF", 1),
                    RequirementDto("A02-2", "Desc", "BF", 2),
                    RequirementDto("A02-3", "Desc", "BF", 3),
                ),
            ),
        )

        override suspend fun getCriteriaForPerson(personId: UUID): List<CriterionDto> = criteria
    }
}

