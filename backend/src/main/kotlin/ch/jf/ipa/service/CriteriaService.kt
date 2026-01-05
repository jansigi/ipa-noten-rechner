package ch.jf.ipa.service

import ch.jf.ipa.dto.CriterionDto
import ch.jf.ipa.util.CriteriaLoader

interface CriteriaProvider {
    fun getAllCriteria(): List<CriterionDto>
}

class CriteriaService : CriteriaProvider {
    private val criteria: List<CriterionDto> by lazy {
        CriteriaLoader.loadCriteria()
    }

    override fun getAllCriteria(): List<CriterionDto> = criteria
}

