package ch.jf.ipa.service

import kotlinx.serialization.Serializable

@Serializable
data class Guetestufe(
    val stufe: Int,
    val bezeichnung: String,
    val regel: String,
    val kriterien: List<String>,
)

@Serializable
data class Kriterium(
    val id: String,
    val titel: String,
    val frage: String,
    val guetestufen: List<Guetestufe>,
)

object KriterienParser {
    fun extractBlocks(text: String): List<String> {
        val lines = text
            .lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        val blocks = mutableListOf<String>()
        val current = mutableListOf<String>()

        for (line in lines) {
            val isNewBlock =
                line.length >= 3 &&
                line[0] in "ABCDEFG" &&
                line.substring(1, 3).all { it.isDigit() } &&
                (line.length == 3 || line[3] == ' ')

            if (isNewBlock && current.isNotEmpty()) {
                blocks += current.joinToString("\n")
                current.clear()
            }
            current += line
        }

        if (current.isNotEmpty()) {
            blocks += current.joinToString("\n")
        }

        return blocks
    }

    fun parseBlock(block: String): Kriterium? {
        val lines = block
            .lineSequence()
            .filter { it.isNotBlank() }
            .toList()

        if (lines.size < 2) return null

        val header = lines[0]
        val id = header.split(" ").first()
        val titel = header.drop(id.length).trim()
        val frage = lines[1].trim()

        val guetestufen = mutableListOf<Guetestufe>()

        var currentStufe: Int? = null
        var currentBezeichnung = ""
        var currentRegel = ""
        val currentKriterien = mutableListOf<String>()

        fun flush() {
            if (currentStufe != null) {
                guetestufen += Guetestufe(
                    stufe = currentStufe!!,
                    bezeichnung = currentBezeichnung,
                    regel = currentRegel.trim(),
                    kriterien = currentKriterien.filter { it.isNotBlank() },
                )
            }
            currentStufe = null
            currentBezeichnung = ""
            currentRegel = ""
            currentKriterien.clear()
        }

        for (line in lines.drop(2)) {
            when {
                line.startsWith("Gütestufe") -> {
                    flush()
                    val parts = line.split(" ")
                    val level = parts.getOrNull(1)?.toIntOrNull() ?: continue
                    currentStufe = level
                    currentBezeichnung = "Gütestufe $level"
                    currentRegel = parts.drop(2).joinToString(" ").trim()
                }

                line.length > 3 && line[0].isDigit() && line.substring(1, 3) == ". " -> {
                    if (currentStufe == null) continue
                    currentKriterien += line.drop(3).trim()
                }

                else -> {
                    if (currentStufe == null) continue
                    if (currentKriterien.isEmpty()) {
                        currentRegel = (currentRegel + " " + line).trim()
                    } else {
                        val last = currentKriterien.removeLast()
                        currentKriterien += (last + " " + line).trim()
                    }
                }
            }
        }

        flush()

        return Kriterium(
            id = id,
            titel = titel,
            frage = frage,
            guetestufen = guetestufen,
        )
    }

    fun parse(text: String): List<Kriterium> {
        return extractBlocks(text).mapNotNull { parseBlock(it) }
    }
}

