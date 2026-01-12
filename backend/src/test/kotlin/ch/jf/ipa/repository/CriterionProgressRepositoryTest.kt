package ch.jf.ipa.repository

import ch.jf.ipa.model.CriterionProgress
import ch.jf.ipa.model.Person
import java.time.LocalDate
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest

class CriterionProgressRepositoryTest : RepositoryTestBase() {
    private lateinit var progressRepository: CriterionProgressRepository
    private lateinit var personRepository: PersonRepository

    @BeforeTest
    fun setup() {
        initDatabase()
        progressRepository = CriterionProgressRepositoryImpl()
        personRepository = PersonRepositoryImpl()
    }

    @Test
    fun createAndUpdateProgress() =
        runTest {
            val personId = UUID.randomUUID()
            val person =
                Person(
                    id = personId,
                    firstName = "Grace",
                    lastName = "Hopper",
                    topic = "Compilers",
                    submissionDate = LocalDate.of(2025, 6, 1),
                )
            personRepository.create(person)

            val progressId = UUID.randomUUID()
            val initialProgress =
                CriterionProgress(
                    id = progressId,
                    personId = personId,
                    criterionId = "criterion-1",
                    checkedRequirements = listOf("req-1"),
                    note = "Initial",
                )

            val created = progressRepository.createOrUpdate(initialProgress)
            assertEquals(initialProgress, created)

            val stored = progressRepository.getByPersonId(personId)
            assertEquals(listOf(initialProgress), stored)

            val updatedProgress =
                initialProgress.copy(
                    checkedRequirements = listOf("req-1", "req-2"),
                    note = "Updated",
                )

            val updated = progressRepository.createOrUpdate(updatedProgress)
            assertEquals(updatedProgress, updated)

            val afterUpdate = progressRepository.getByPersonId(personId)
            assertEquals(listOf(updatedProgress), afterUpdate)
        }
}
