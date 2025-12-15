package ch.jf.ipa.dto

import ch.jf.ipa.model.CriterionProgress
import java.util.UUID
import kotlinx.serialization.Serializable

@Serializable
data class CriterionProgressResponseDto(
    val id: String,
    val personId: String,
    val criterionId: String,
    val checkedRequirements: List<String>,
    val note: String?,
)

@Serializable
data class CriterionProgressRequest(
    val id: String? = null,
    val criterionId: String,
    val checkedRequirements: List<String> = emptyList(),
    val note: String? = null,
)

fun CriterionProgress.toResponseDto(): CriterionProgressResponseDto = CriterionProgressResponseDto(
    id = id.toString(),
    personId = personId.toString(),
    criterionId = criterionId,
    checkedRequirements = checkedRequirements,
    note = note,
)

fun CriterionProgressRequest.toDomain(personId: UUID): CriterionProgress = CriterionProgress(
    id = id?.let(UUID::fromString) ?: UUID.randomUUID(),
    personId = personId,
    criterionId = criterionId,
    checkedRequirements = checkedRequirements,
    note = note,
)

