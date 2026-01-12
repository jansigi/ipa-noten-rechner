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
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class IpaRepositoryImpl : IpaRepository {
    private val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
    }

    override suspend fun replaceActiveDataset(dataset: IpaDatasetDto): IpaDataset {
        return DatabaseFactory.dbQuery {
            clearInternal()
            val id = UUID.randomUUID()
            val now = Instant.now()
            val serialized = json.encodeToString(dataset)
            IpaDatasetsTable.insert {
                it[IpaDatasetsTable.id] = id
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

    override suspend fun getActiveDataset(): IpaDataset? = DatabaseFactory.dbQuery {
        IpaDatasetsTable.selectAll()
            .limit(1)
            .singleOrNull()
            ?.toDataset()
    }

    override suspend fun clear() {
        DatabaseFactory.dbQuery {
            clearInternal()
        }
    }

    private fun clearInternal() {
        IpaDatasetsTable.deleteAll()
    }

    private fun ResultRow.toDataset(): IpaDataset = IpaDataset(
        id = this[IpaDatasetsTable.id].value,
        ipaName = this[IpaDatasetsTable.ipaName],
        topic = this[IpaDatasetsTable.topic],
        candidateFullName = this[IpaDatasetsTable.candidateFullName],
        candidateFirstName = this[IpaDatasetsTable.candidateFirstName],
        candidateLastName = this[IpaDatasetsTable.candidateLastName],
        rawJson = this[IpaDatasetsTable.rawJson],
        createdAt = this[IpaDatasetsTable.createdAt],
    )
}

