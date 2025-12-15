package ch.jf.ipa.dto

import ch.jf.ipa.model.Person
import java.time.LocalDate
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class PersonResponseDto(
    val id: String,
    val firstName: String,
    val lastName: String,
    val topic: String,
    val submissionDate: String,
)

@Serializable
data class CreatePersonRequest(
    val firstName: String,
    val lastName: String,
    val topic: String,
    val submissionDate: String,
    val id: String? = null,
)

fun Person.toResponseDto(): PersonResponseDto = PersonResponseDto(
    id = id.toString(),
    firstName = firstName,
    lastName = lastName,
    topic = topic,
    submissionDate = submissionDate.toString(),
)

fun CreatePersonRequest.toPerson(): Person = Person(
    id = id?.let(UUID::fromString) ?: UUID.randomUUID(),
    firstName = firstName,
    lastName = lastName,
    topic = topic,
    submissionDate = LocalDate.parse(submissionDate),
)

