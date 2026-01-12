package ch.jf.ipa.service

import ch.jf.ipa.dto.AppMetadataDto
import ch.jf.ipa.model.IpaDataset
import ch.jf.ipa.repository.MetadataRepository

private const val IPA_NAME_KEY = "ipa_name"
private const val IPA_TOPIC_KEY = "ipa_topic"
private const val CANDIDATE_FULL_NAME_KEY = "candidate_full_name"
private const val CANDIDATE_FIRST_NAME_KEY = "candidate_first_name"
private const val CANDIDATE_LAST_NAME_KEY = "candidate_last_name"
private const val ACTIVE_DATASET_ID_KEY = "active_dataset_id"

class MetadataService(
    private val metadataRepository: MetadataRepository,
) {
    suspend fun getMetadata(): AppMetadataDto = AppMetadataDto(
        ipaName = metadataRepository.getValue(IPA_NAME_KEY),
        topic = metadataRepository.getValue(IPA_TOPIC_KEY),
        candidateFullName = metadataRepository.getValue(CANDIDATE_FULL_NAME_KEY),
        candidateFirstName = metadataRepository.getValue(CANDIDATE_FIRST_NAME_KEY),
        candidateLastName = metadataRepository.getValue(CANDIDATE_LAST_NAME_KEY),
        activeDatasetId = metadataRepository.getValue(ACTIVE_DATASET_ID_KEY),
    )

    suspend fun setActiveDataset(dataset: IpaDataset) {
        metadataRepository.setValue(ACTIVE_DATASET_ID_KEY, dataset.id.toString())
        metadataRepository.setValue(IPA_NAME_KEY, dataset.ipaName)
        metadataRepository.setValue(IPA_TOPIC_KEY, dataset.topic)
        metadataRepository.setValue(CANDIDATE_FULL_NAME_KEY, dataset.candidateFullName)
        metadataRepository.setValue(CANDIDATE_FIRST_NAME_KEY, dataset.candidateFirstName)
        metadataRepository.setValue(CANDIDATE_LAST_NAME_KEY, dataset.candidateLastName)
    }

    suspend fun clear() {
        metadataRepository.setValue(ACTIVE_DATASET_ID_KEY, null)
        metadataRepository.setValue(IPA_NAME_KEY, null)
        metadataRepository.setValue(IPA_TOPIC_KEY, null)
        metadataRepository.setValue(CANDIDATE_FULL_NAME_KEY, null)
        metadataRepository.setValue(CANDIDATE_FIRST_NAME_KEY, null)
        metadataRepository.setValue(CANDIDATE_LAST_NAME_KEY, null)
    }
}

