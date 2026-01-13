package ch.jf.ipa.model

import java.util.UUID
import org.jetbrains.exposed.dao.id.UUIDTable

data class CriterionProgress(
    val id: UUID,
    val personId: UUID,
    val criterionId: String,
    val checkedRequirements: List<String>,
    val note: String?,
)

object CriterionProgressTable : UUIDTable("criterion_progress") {
    val personId = uuid("person_id")
    val criterionId = varchar("criterion_id", 255)
    val checkedRequirements = text("checked_requirements")
    val note = text("note").nullable()
}
