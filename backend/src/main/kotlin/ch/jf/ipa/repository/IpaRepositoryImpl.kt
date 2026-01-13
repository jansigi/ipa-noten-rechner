package ch.jf.ipa.repository

import ch.jf.ipa.config.DatabaseFactory
import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.model.IpaDataset
import ch.jf.ipa.model.IpaDatasetsTable
import java.time.Instant
import java.util.UUID
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class IpaRepositoryImpl : IpaRepository {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    override suspend fun createForPerson(personId: UUID, dataset: IpaDatasetDto): IpaDataset {
        return DatabaseFactory.dbQuery {
            val id = UUID.randomUUID()
            val now = Instant.now()
            val serialized = json.encodeToString(dataset)

            IpaDatasetsTable.insert {
                it[IpaDatasetsTable.id] = id
                it[IpaDatasetsTable.personId] = personId
                it[ipaName] = dataset.ipaName
                it[topic] = dataset.topic
                it[candidateFullName] = dataset.candidate?.fullName
                it[candidateFirstName] = dataset.candidate?.firstName
                it[candidateLastName] = dataset.candidate?.lastName
                it[rawJson] = serialized
                it[createdAt] = now
            }

            IpaDataset(
                id = id,
                personId = personId,
                ipaName = dataset.ipaName,
                topic = dataset.topic,
                candidateFullName = dataset.candidate?.fullName,
                candidateFirstName = dataset.candidate?.firstName,
                candidateLastName = dataset.candidate?.lastName,
                rawJson = serialized,
                createdAt = now,
            )
        }
    }

    override suspend fun getForPerson(personId: UUID): IpaDataset? = DatabaseFactory.dbQuery {
        IpaDatasetsTable
            .select { IpaDatasetsTable.personId eq personId }
            .orderBy(IpaDatasetsTable.createdAt to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.toDataset()
    }

    override suspend fun getLatest(): IpaDataset? = DatabaseFactory.dbQuery {
        IpaDatasetsTable
            .selectAll()
            .orderBy(IpaDatasetsTable.createdAt to SortOrder.DESC)
            .limit(1)
            .singleOrNull()
            ?.toDataset()
    }

    override suspend fun clearAll() {
        DatabaseFactory.dbQuery {
            IpaDatasetsTable.deleteAll()
        }
    }

    private fun ResultRow.toDataset(): IpaDataset = IpaDataset(
        id = this[IpaDatasetsTable.id].value,
        personId = this[IpaDatasetsTable.personId],
        ipaName = this[IpaDatasetsTable.ipaName],
        topic = this[IpaDatasetsTable.topic],
        candidateFullName = this[IpaDatasetsTable.candidateFullName],
        candidateFirstName = this[IpaDatasetsTable.candidateFirstName],
        candidateLastName = this[IpaDatasetsTable.candidateLastName],
        rawJson = this[IpaDatasetsTable.rawJson],
        createdAt = this[IpaDatasetsTable.createdAt],
    )
}
