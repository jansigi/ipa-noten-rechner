package ch.jf.ipa.service

import ch.jf.ipa.dto.CriterionResultDto
import ch.jf.ipa.dto.PersonResultsDto
import ch.jf.ipa.repository.CriterionProgressRepository
import java.util.UUID
import kotlin.math.ceil

class GradingService(
    private val criteriaProvider: PersonCriteriaProvider,
    private val progressRepository: CriterionProgressRepository,
) {

    suspend fun calculateResultsForPerson(personId: UUID): PersonResultsDto {
        val criteria = criteriaProvider.getCriteriaForPerson(personId)
        val progressByCriterion = progressRepository.getByPersonId(personId).associateBy { it.criterionId }

        val results = criteria.map { criterion ->
            val requirementIds = criterion.requirements.map { it.id }.toSet()
            val totalCount = requirementIds.size

            val progress = progressByCriterion[criterion.id]
            val checked = progress?.checkedRequirements.orEmpty().filter { it in requirementIds }
            val fulfilledCount = checked.size
            val gradeLevel = calculateGradeLevel(fulfilledCount, totalCount)

            CriterionResultDto(
                criterionId = criterion.id,
                fulfilledCount = fulfilledCount,
                totalCount = totalCount,
                gradeLevel = gradeLevel,
                checkedRequirements = checked,
                note = progress?.note,
                title = criterion.title,
            )
        }.sortedBy { it.criterionId }

        return PersonResultsDto(
            personId = personId,
            results = results,
        )
    }

    companion object {
        fun calculateGradeLevel(fulfilled: Int, total: Int): Int {
            if (total <= 0) {
                return 0
            }
            if (fulfilled <= 0) {
                return 0
            }
            if (fulfilled >= total) {
                return 3
            }

            val twoThirdThreshold = maxOf(
                ceil(total * 2 / 3.0).toInt(),
                total - 2,
            )
            if (fulfilled >= twoThirdThreshold && twoThirdThreshold < total) {
                return 2
            }

            val oneThirdThreshold = maxOf(ceil(total / 3.0).toInt(), 1)
            if (fulfilled >= oneThirdThreshold) {
                return 1
            }

            return 0
        }
    }
}

