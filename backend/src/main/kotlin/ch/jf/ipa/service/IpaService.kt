package ch.jf.ipa.service

import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.repository.IpaRepository
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class IpaService(
    private val ipaRepository: IpaRepository,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getActiveDataset(): IpaDatasetDto? {
        val stored = ipaRepository.getActiveDataset() ?: return null
        return runCatching { json.decodeFromString<IpaDatasetDto>(stored.rawJson) }.getOrNull()
    }
}

