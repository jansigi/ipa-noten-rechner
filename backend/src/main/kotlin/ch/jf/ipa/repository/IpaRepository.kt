package ch.jf.ipa.repository

import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.model.IpaDataset

interface IpaRepository {
    suspend fun replaceActiveDataset(dataset: IpaDatasetDto): IpaDataset
    suspend fun getActiveDataset(): IpaDataset?
    suspend fun clear()
}

