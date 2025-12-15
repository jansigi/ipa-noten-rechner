package ch.jf.ipa.dto

import kotlinx.serialization.Serializable

@Serializable
data class CriterionDto(
    val id: String,
    val title: String,
    val question: String,
    val requirements: List<RequirementDto>,
)

@Serializable
data class RequirementDto(
    val id: String,
    val description: String,
    val module: String,
    val part: Int,
)
