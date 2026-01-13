package ch.jf.ipa.service

import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.repository.IpaRepository
import java.util.UUID
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class IpaService(
    private val ipaRepository: IpaRepository,
) {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    suspend fun getDatasetForPerson(personId: UUID): IpaDatasetDto? {
        val stored = ipaRepository.getForPerson(personId) ?: return null
        return runCatching { json.decodeFromString<IpaDatasetDto>(stored.rawJson) }.getOrNull()
    }

    suspend fun getLatestDataset(): IpaDatasetDto? {
        val stored = ipaRepository.getLatest() ?: return null
        return runCatching { json.decodeFromString<IpaDatasetDto>(stored.rawJson) }.getOrNull()
    }
}
