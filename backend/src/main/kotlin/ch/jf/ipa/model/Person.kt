package ch.jf.ipa.model

import java.time.LocalDate
import java.util.UUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.date

data class Person(
    val id: UUID,
    val firstName: String,
    val lastName: String,
    val topic: String,
    val submissionDate: LocalDate,
)

object PersonsTable : UUIDTable("persons") {
    val firstName = varchar("first_name", 255)
    val lastName = varchar("last_name", 255)
    val topic = varchar("topic", 255)
    val submissionDate = date("submission_date")
}
