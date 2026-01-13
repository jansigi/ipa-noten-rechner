package ch.jf.ipa.dto

import kotlinx.serialization.Serializable

@Serializable
data class AppMetadataDto(
    val ipaName: String?,
    val topic: String?,
    val candidateFullName: String?,
    val candidateFirstName: String?,
    val candidateLastName: String?,
    val activeDatasetId: String?,
)

@Serializable
data class IpaImportResponseDto(
    val datasetId: String,
    val personId: String,
    val ipaName: String?,
    val topic: String?,
    val candidateFullName: String?,
    val criteriaCount: Int,
)

