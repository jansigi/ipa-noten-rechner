package ch.jf.ipa.repository

import ch.jf.ipa.dto.CriterionDto

interface CriteriaRepository {
    suspend fun getAll(): List<CriterionDto>

    suspend fun replaceAll(criteria: List<CriterionDto>)
}
