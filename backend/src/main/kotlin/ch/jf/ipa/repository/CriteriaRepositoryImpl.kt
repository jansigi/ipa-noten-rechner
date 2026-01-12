package ch.jf.ipa.repository

import ch.jf.ipa.config.DatabaseFactory
import ch.jf.ipa.dto.CriterionDto
import ch.jf.ipa.dto.RequirementDto
import ch.jf.ipa.model.CriteriaTable
import ch.jf.ipa.model.CriterionRequirementsTable
import ch.jf.ipa.util.CriteriaLoader
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll

class CriteriaRepositoryImpl : CriteriaRepository {
    override suspend fun getAll(): List<CriterionDto> = DatabaseFactory.dbQuery {
        seedIfEmpty()
        val requirementRows = CriterionRequirementsTable
            .selectAll()
            .orderBy(CriterionRequirementsTable.criterionId to SortOrder.ASC, CriterionRequirementsTable.part to SortOrder.ASC)
            .map { it }
        val requirementsByCriterion = requirementRows
            .groupBy { it[CriterionRequirementsTable.criterionId] }
            .mapValues { (_, rows) ->
                rows.map { row ->
                    RequirementDto(
                        id = row[CriterionRequirementsTable.id],
                        description = row[CriterionRequirementsTable.description],
                        module = row[CriterionRequirementsTable.module],
                        part = row[CriterionRequirementsTable.part],
                    )
                }
            }

        CriteriaTable
            .selectAll()
            .orderBy(CriteriaTable.id to SortOrder.ASC)
            .map { row ->
                val criterionId = row[CriteriaTable.id]
                CriterionDto(
                    id = criterionId,
                    title = row[CriteriaTable.title],
                    question = row[CriteriaTable.question],
                    requirements = requirementsByCriterion[criterionId].orEmpty(),
                )
            }
    }

    override suspend fun replaceAll(criteria: List<CriterionDto>) {
        DatabaseFactory.dbQuery {
            CriterionRequirementsTable.deleteAll()
            CriteriaTable.deleteAll()
            insertCriteria(criteria)
        }
    }

    private fun org.jetbrains.exposed.sql.Transaction.insertCriteria(criteria: List<CriterionDto>) {
        if (criteria.isEmpty()) {
            return
        }

        CriteriaTable.batchInsert(criteria) { criterion ->
            this[CriteriaTable.id] = criterion.id
            this[CriteriaTable.title] = criterion.title
            this[CriteriaTable.question] = criterion.question
        }

        CriterionRequirementsTable.batchInsert(criteria.flatMap { criterion ->
            criterion.requirements.map { criterion.id to it }
        }) { (criterionId, requirement) ->
            this[CriterionRequirementsTable.id] = requirement.id
            this[CriterionRequirementsTable.criterionId] = criterionId
            this[CriterionRequirementsTable.description] = requirement.description
            this[CriterionRequirementsTable.module] = requirement.module
            this[CriterionRequirementsTable.part] = requirement.part
        }
    }

    private fun org.jetbrains.exposed.sql.Transaction.seedIfEmpty() {
        val isEmpty = CriteriaTable
            .selectAll()
            .limit(1)
            .empty()

        if (!isEmpty) {
            return
        }

        val fallbackCriteria = CriteriaLoader.loadCriteria()
        insertCriteria(fallbackCriteria)
    }
}

