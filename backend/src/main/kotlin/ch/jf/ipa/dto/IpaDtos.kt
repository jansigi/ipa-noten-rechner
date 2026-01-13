package ch.jf.ipa.dto

import kotlinx.serialization.Serializable

@Serializable
data class CandidateDto(
    val fullName: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
)

@Serializable
data class IpaDatasetDto(
    val ipaName: String? = null,
    val topic: String? = null,
    val candidate: CandidateDto? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val criteria: List<CriterionDto> = emptyList(),
)

