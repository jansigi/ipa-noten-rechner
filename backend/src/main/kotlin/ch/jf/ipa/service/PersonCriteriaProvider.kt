package ch.jf.ipa.service

import ch.jf.ipa.dto.CriterionDto
import java.util.UUID

interface PersonCriteriaProvider {
    suspend fun getCriteriaForPerson(personId: UUID): List<CriterionDto>
}

class IpaPersonCriteriaProvider(
    private val ipaService: IpaService,
) : PersonCriteriaProvider {
    override suspend fun getCriteriaForPerson(personId: UUID): List<CriterionDto> {
        return ipaService.getDatasetForPerson(personId)?.criteria.orEmpty()
    }
}
