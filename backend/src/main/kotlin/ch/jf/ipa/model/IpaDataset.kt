package ch.jf.ipa.model

import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.timestamp

data class IpaDataset(
    val id: UUID,
    val ipaName: String?,
    val topic: String?,
    val candidateFullName: String?,
    val candidateFirstName: String?,
    val candidateLastName: String?,
    val rawJson: String,
    val createdAt: Instant,
)

object IpaDatasetsTable : UUIDTable("ipa_datasets") {
    val ipaName = varchar("ipa_name", 255).nullable()
    val topic = varchar("topic", 255).nullable()
    val candidateFullName = varchar("candidate_full_name", 255).nullable()
    val candidateFirstName = varchar("candidate_first_name", 255).nullable()
    val candidateLastName = varchar("candidate_last_name", 255).nullable()
    val rawJson = text("raw_json")
    val createdAt = timestamp("created_at")
}

