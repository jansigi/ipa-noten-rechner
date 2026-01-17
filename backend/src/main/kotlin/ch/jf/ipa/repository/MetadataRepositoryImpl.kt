package ch.jf.ipa.repository

import ch.jf.ipa.config.DatabaseFactory
import ch.jf.ipa.model.MetadataTable
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class MetadataRepositoryImpl : MetadataRepository {
    override suspend fun getValue(key: String): String? =
        DatabaseFactory.dbQuery {
            MetadataTable
                .select { MetadataTable.key eq key }
                .limit(1)
                .singleOrNull()
                ?.get(MetadataTable.value)
        }

    override suspend fun setValue(
        key: String,
        value: String?,
    ) {
        DatabaseFactory.dbQuery {
            val existing =
                MetadataTable
                    .select { MetadataTable.key eq key }
                    .limit(1)
                    .singleOrNull()

            if (existing == null) {
                MetadataTable.insert {
                    it[MetadataTable.key] = key
                    it[MetadataTable.value] = value
                }
            } else {
                if (value == null) {
                    MetadataTable.deleteWhere { MetadataTable.key eq key }
                } else {
                    MetadataTable.update({ MetadataTable.key eq key }) {
                        it[MetadataTable.value] = value
                    }
                }
            }
        }
    }
}
