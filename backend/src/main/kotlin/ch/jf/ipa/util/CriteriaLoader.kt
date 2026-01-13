package ch.jf.ipa.util

import ch.jf.ipa.dto.CriterionDto
import java.io.InputStreamReader
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

object CriteriaLoader {
    private val json =
        Json {
            ignoreUnknownKeys = true
        }

    fun loadCriteria(): List<CriterionDto> {
        val resource =
            CriteriaLoader::class.java.classLoader?.getResource("criteria.json")
                ?: return emptyList()

        resource.openStream().use { stream ->
            InputStreamReader(stream).use { reader ->
                val content = reader.readText()
                if (content.isBlank()) {
                    return emptyList()
                }
                return json.decodeFromString(ListSerializer(CriterionDto.serializer()), content)
            }
        }
    }
}
