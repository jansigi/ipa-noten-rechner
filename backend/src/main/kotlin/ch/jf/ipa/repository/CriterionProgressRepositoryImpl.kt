package ch.jf.ipa.repository

import ch.jf.ipa.config.DatabaseFactory
import ch.jf.ipa.model.CriterionProgress
import ch.jf.ipa.model.CriterionProgressTable
import java.util.UUID
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update

class CriterionProgressRepositoryImpl : CriterionProgressRepository {
    private val json = Json { ignoreUnknownKeys = true }
    private val listSerializer = ListSerializer(String.serializer())

    override suspend fun createOrUpdate(progress: CriterionProgress): CriterionProgress =
        DatabaseFactory.dbQuery {
            val serializedRequirements = json.encodeToString(listSerializer, progress.checkedRequirements)
            val existing =
                CriterionProgressTable.select { CriterionProgressTable.id eq progress.id }
                    .singleOrNull()

            if (existing == null) {
                CriterionProgressTable.insert {
                    it[id] = progress.id
                    it[personId] = progress.personId
                    it[criterionId] = progress.criterionId
                    it[checkedRequirements] = serializedRequirements
                    it[note] = progress.note
                }
            } else {
                CriterionProgressTable.update({ CriterionProgressTable.id eq progress.id }) {
                    it[personId] = progress.personId
                    it[criterionId] = progress.criterionId
                    it[checkedRequirements] = serializedRequirements
                    it[note] = progress.note
                }
            }

            CriterionProgressTable.select { CriterionProgressTable.id eq progress.id }
                .single()
                .toProgress()
        }

    override suspend fun getByPersonId(personId: UUID): List<CriterionProgress> =
        DatabaseFactory.dbQuery {
            CriterionProgressTable.select { CriterionProgressTable.personId eq personId }
                .map { it.toProgress() }
        }

    private fun ResultRow.toProgress(): CriterionProgress {
        val requirementsRaw = this[CriterionProgressTable.checkedRequirements]
        val requirements =
            requirementsRaw.takeIf { it.isNotBlank() }
                ?.let { json.decodeFromString(listSerializer, it) }
                ?: emptyList()

        return CriterionProgress(
            id = this[CriterionProgressTable.id].value,
            personId = this[CriterionProgressTable.personId],
            criterionId = this[CriterionProgressTable.criterionId],
            checkedRequirements = requirements,
            note = this[CriterionProgressTable.note],
        )
    }
}
