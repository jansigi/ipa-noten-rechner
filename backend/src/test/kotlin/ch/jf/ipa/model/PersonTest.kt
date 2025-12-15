package ch.jf.ipa.model

import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class PersonTest {
    @Test
    fun personInstantiates() {
        val id = UUID.randomUUID()
        val person = Person(
            id = id,
            firstName = "Max",
            lastName = "Mustermann",
            topic = "Ktor Backend",
            submissionDate = LocalDate.of(2025, 1, 1),
        )

        assertEquals(id, person.id)
        assertEquals("Max", person.firstName)
        assertEquals("Mustermann", person.lastName)
    }
}

