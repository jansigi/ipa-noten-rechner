package ch.jf.ipa.repository

import ch.jf.ipa.config.DatabaseFactory
import ch.jf.ipa.dto.CriterionDto
import ch.jf.ipa.model.CriteriaTable
import ch.jf.ipa.model.CriterionRequirementsTable
import ch.jf.ipa.util.CriteriaLoader
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.selectAll

class CriteriaRepositoryImpl : CriteriaRepository {
    override suspend fun getAll(): List<CriterionDto> {
        val catalog = CriteriaLoader.loadCriteria()
        if (catalog.isNotEmpty()) {
            return catalog
        }

        // Fallback: if the resource is missing, return whatever is in DB.
        return DatabaseFactory.dbQuery {
            CriteriaTable.selectAll().map { row ->
                CriterionDto(
                    id = row[CriteriaTable.id],
                    title = row[CriteriaTable.title],
                    question = row[CriteriaTable.question],
                    requirements = emptyList(),
                )
            }
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

        CriterionRequirementsTable.batchInsert(
            criteria.flatMap { criterion ->
                criterion.requirements.map { criterion.id to it }
            },
        ) { (criterionId, requirement) ->
            this[CriterionRequirementsTable.id] = requirement.id
            this[CriterionRequirementsTable.criterionId] = criterionId
            this[CriterionRequirementsTable.description] = requirement.description
            this[CriterionRequirementsTable.module] = requirement.module
            this[CriterionRequirementsTable.part] = requirement.part
        }
    }
}
