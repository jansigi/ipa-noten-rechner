package ch.jf.ipa.service

import ch.jf.ipa.dto.EvaluatedCriterionDto
import ch.jf.ipa.repository.CriterionProgressRepository
import java.util.UUID

class EvaluationService(
    private val progressRepository: CriterionProgressRepository,
    private val criteriaProvider: CriteriaProvider,
) {
    suspend fun evaluate(personId: UUID): List<EvaluatedCriterionDto> {
        val criteria = criteriaProvider.getAllCriteria()
        val progressByCriterion = progressRepository
            .getByPersonId(personId)
            .associateBy { it.criterionId }

        return criteria.map { criterion ->
            val totalCount = criterion.requirements.size
            val progress = progressByCriterion[criterion.id]
            val checked = progress?.checkedRequirements?.toSet().orEmpty()
            val checkedCount = checked.count { id -> criterion.requirements.any { it.id == id } }

            val grade = calculateGrade(checkedCount, totalCount)

            EvaluatedCriterionDto(
                criterionId = criterion.id,
                totalRequirements = totalCount,
                checkedRequirements = checkedCount,
                grade = grade,
            )
        }
    }

    private fun calculateGrade(checked: Int, total: Int): Int {
        if (total <= 0) return 0
        if (checked >= total) return 3

        val ratio = checked.toDouble() / total.toDouble()
        return when {
            ratio >= 2.0 / 3.0 -> 2
            ratio >= 1.0 / 3.0 -> 1
            else -> 0
        }
    }
}

