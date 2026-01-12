package ch.jf.ipa.model

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

data class Criterion(
    val id: String,
    val title: String,
    val question: String,
    val requirements: List<CriterionRequirement>,
)

data class CriterionRequirement(
    val id: String,
    val criterionId: String,
    val description: String,
    val module: String,
    val part: Int,
)

object CriteriaTable : Table("criteria") {
    val id = varchar("id", length = 32)
    val title = text("title")
    val question = text("question")

    override val primaryKey = PrimaryKey(id)
}

object CriterionRequirementsTable : Table("criterion_requirements") {
    val id = varchar("id", length = 64)
    val criterionId = varchar("criterion_id", length = 32).references(
        ref = CriteriaTable.id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
    )
    val description = text("description")
    val module = varchar("module", length = 32)
    val part = integer("part")

    override val primaryKey = PrimaryKey(id)
}

