package ch.jf.ipa.repository

import ch.jf.ipa.config.DatabaseFactory
import ch.jf.ipa.model.Person
import ch.jf.ipa.model.PersonsTable
import java.util.UUID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class PersonRepositoryImpl : PersonRepository {
    override suspend fun create(person: Person): Person = DatabaseFactory.dbQuery {
        val insertStatement = PersonsTable.insert {
            it[id] = person.id
            it[firstName] = person.firstName
            it[lastName] = person.lastName
            it[topic] = person.topic
            it[submissionDate] = person.submissionDate
        }
        insertStatement.resultedValues?.singleOrNull()?.toPerson() ?: person
    }

    override suspend fun getAll(): List<Person> = DatabaseFactory.dbQuery {
        PersonsTable.selectAll().map { it.toPerson() }
    }

    override suspend fun getById(id: UUID): Person? = DatabaseFactory.dbQuery {
        PersonsTable.select { PersonsTable.id eq id }
            .singleOrNull()
            ?.toPerson()
    }

    private fun ResultRow.toPerson(): Person = Person(
        id = this[PersonsTable.id].value,
        firstName = this[PersonsTable.firstName],
        lastName = this[PersonsTable.lastName],
        topic = this[PersonsTable.topic],
        submissionDate = this[PersonsTable.submissionDate],
    )
}

