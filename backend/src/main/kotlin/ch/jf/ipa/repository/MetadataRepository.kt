package ch.jf.ipa.repository

interface MetadataRepository {
    suspend fun getValue(key: String): String?

    suspend fun setValue(
        key: String,
        value: String?,
    )
}
