package ch.jf.ipa.repository

import ch.jf.ipa.model.Person
import java.util.UUID

interface PersonRepository {
    suspend fun create(person: Person): Person
    suspend fun getAll(): List<Person>
    suspend fun getById(id: UUID): Person?
}

