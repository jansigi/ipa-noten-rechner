package ch.jf.ipa.dto

import kotlinx.serialization.Serializable

@Serializable
data class EvaluatedCriterionDto(
    val criterionId: String,
    val totalRequirements: Int,
    val checkedRequirements: Int,
    val grade: Int,
)

