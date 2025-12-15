package ch.ergon.ipa.dto

import kotlinx.serialization.Serializable

@Serializable
data class CriterionDto(
    val id: String,
    val title: String,
    val requirements: List<String>,
)

