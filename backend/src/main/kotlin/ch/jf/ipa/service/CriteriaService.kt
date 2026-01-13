package ch.jf.ipa.service

import ch.jf.ipa.dto.CriterionDto
import ch.jf.ipa.repository.CriteriaRepository

interface CriteriaProvider {
    suspend fun getAllCriteria(): List<CriterionDto>
}

class CriteriaService(
    private val repository: CriteriaRepository,
) : CriteriaProvider {
    override suspend fun getAllCriteria(): List<CriterionDto> = repository.getAll()
}
