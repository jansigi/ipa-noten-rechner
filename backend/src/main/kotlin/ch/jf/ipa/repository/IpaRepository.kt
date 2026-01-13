package ch.jf.ipa.repository

import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.model.IpaDataset
import java.util.UUID

interface IpaRepository {
    suspend fun createForPerson(personId: UUID, dataset: IpaDatasetDto): IpaDataset
    suspend fun getForPerson(personId: UUID): IpaDataset?
    suspend fun getLatest(): IpaDataset?
    suspend fun clearAll()
}

