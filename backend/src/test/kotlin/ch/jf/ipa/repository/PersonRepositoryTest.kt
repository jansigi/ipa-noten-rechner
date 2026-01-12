package ch.jf.ipa.repository

import ch.jf.ipa.model.Person
import java.time.LocalDate
import java.util.UUID
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.test.runTest

class PersonRepositoryTest : RepositoryTestBase() {
    private lateinit var repository: PersonRepository

    @BeforeTest
    fun setup() {
        initDatabase()
        repository = PersonRepositoryImpl()
    }

    @Test
    fun createAndRetrievePerson() =
        runTest {
            val id = UUID.randomUUID()
            val person =
                Person(
                    id = id,
                    firstName = "Ada",
                    lastName = "Lovelace",
                    topic = "Analytical Engine",
                    submissionDate = LocalDate.of(2025, 5, 1),
                )

            val created = repository.create(person)
            assertEquals(person, created)

            val allPersons = repository.getAll()
            assertEquals(1, allPersons.size)
            assertEquals(person, allPersons.first())

            val byId = repository.getById(id)
            assertNotNull(byId)
            assertEquals(person, byId)
        }
}
