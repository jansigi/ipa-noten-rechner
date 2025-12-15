package ch.jf.ipa.dto

import java.util.UUID
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class CriterionResultDto(
    val criterionId: String,
    val fulfilledCount: Int,
    val totalCount: Int,
    val gradeLevel: Int,
    val checkedRequirements: List<String>,
    val note: String?,
    val title: String,
)

@Serializable
data class PersonResultsDto(
    @Serializable(with = UUIDSerializer::class)
    val personId: UUID,
    val results: List<CriterionResultDto>,
)

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

