package ch.jf.ipa.model

import org.jetbrains.exposed.sql.Table

object MetadataTable : Table("metadata") {
    val key = varchar("key", length = 128)
    val value = text("value").nullable()

    override val primaryKey = PrimaryKey(key)
}

