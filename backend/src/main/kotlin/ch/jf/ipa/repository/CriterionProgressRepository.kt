package ch.jf.ipa.repository

import ch.jf.ipa.model.CriterionProgress
import java.util.UUID

interface CriterionProgressRepository {
    suspend fun createOrUpdate(progress: CriterionProgress): CriterionProgress
    suspend fun getByPersonId(personId: UUID): List<CriterionProgress>
}

