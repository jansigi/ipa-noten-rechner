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
    private val blockTokenRegex = Regex("^[A-Za-zÄÖÜ&]{1,8}\\d{1,2}$")
    private val requirementSplitRegex = Regex("""\d+\.\s+""")
    private val footerMarkers = listOf("© 2017", "Inf Api ZH", "Informatiker:in EFZ", "Aufgabenstellung Kandidat/in", "Seite ")

    fun parse(text: String): List<Kriterium> {
        return extractBlocks(text)
            .mapNotNull { parseBlock(it) }
    }

    fun extractBlocks(text: String): List<String> {
        val blocks = mutableListOf<String>()
        val current = mutableListOf<String>()

        sanitizedLines(text).forEach { line ->
            val token = firstToken(line)
            val isNewBlock = token?.let { isBlockToken(it) } ?: false
            if (isNewBlock && current.isNotEmpty()) {
                blocks += current.joinToString("\n")
                current.clear()
            }
            if (line.isNotBlank()) {
                current += line
            }
        }

        if (current.isNotEmpty()) {
            blocks += current.joinToString("\n")
        }

        return blocks
    }

    fun parseBlock(block: String): Kriterium? {
        val lines =
            block.lineSequence()
                .map { sanitizeLine(it) }
                .map { it.replace("|", " ").trim() }
                .filter { it.isNotBlank() && it.any { ch -> !ch.isWhitespace() } }
                .filterNot { it.replace("-", "").isBlank() }
                .toList()

        if (lines.isEmpty()) return null

        val header = lines.first()
        val headerToken = firstToken(header)?.trimEnd(':') ?: return null
        if (!isBlockToken(headerToken)) return null

        val titlePart =
            header.removePrefix(lines.first().split("\\s+".toRegex(), limit = 2).first())
                .trim()
        val title = titlePart.substringAfter(":", titlePart).trim()

        val bodyLines = lines.drop(1)
        val filteredBody = bodyLines.filterNot { it.startsWith("|") }

        val firstGIndex = filteredBody.indexOfFirst { it.startsWith("Gütestufe", ignoreCase = true) }
        val questionLines = if (firstGIndex == -1) filteredBody else filteredBody.take(firstGIndex)
        val question = questionLines.joinToString(" ").trim()

        val detailLines = if (firstGIndex == -1) emptyList() else filteredBody.drop(firstGIndex)
        val guetestufen = parseGütestufen(detailLines)

        return Kriterium(
            id = headerToken,
            titel = title,
            frage = question,
            guetestufen = guetestufen,
        )
    }

    private fun parseGütestufen(lines: List<String>): List<Guetestufe> {
        if (lines.isEmpty()) return emptyList()

        val result = mutableListOf<Guetestufe>()

        var currentStufe: Int? = null
        var currentBezeichnung = ""
        var currentRegel = ""
        val currentKriterien = mutableListOf<String>()

        fun flush() {
            if (currentStufe != null) {
                result +=
                    Guetestufe(
                        stufe = currentStufe!!,
                        bezeichnung = currentBezeichnung.ifBlank { "Gütestufe ${currentStufe!!}" },
                        regel = currentRegel.trim(),
                        kriterien = currentKriterien.map { it.trim() }.filter { it.isNotEmpty() },
                    )
            }
            currentStufe = null
            currentBezeichnung = ""
            currentRegel = ""
            currentKriterien.clear()
        }

        var index = 0
        while (index < lines.size) {
            val line = lines[index]
            when {
                line.startsWith("Gütestufe", ignoreCase = true) -> {
                    flush()
                    val levelMatch = Regex("(?i)gütestufe\\s+(\\d+)").find(line)
                    val level = levelMatch?.groupValues?.getOrNull(1)?.toIntOrNull()
                    if (level == null) {
                        index++
                        continue
                    }
                    currentStufe = level
                    currentBezeichnung = levelMatch.value.trim()
                    val remainder = line.substring(levelMatch.range.last + 1).trim()
                    val (regel, inlineCriteria) = splitRuleAndCriteria(remainder)
                    currentRegel = regel
                    currentKriterien.addAll(inlineCriteria)
                }

                line.length > 2 && line[0].isDigit() && line[1] == '.' -> {
                    currentKriterien += cleanRequirementText(line.substring(2).trim())
                }

                else -> {
                    if (currentStufe != null) {
                        if (currentKriterien.isEmpty()) {
                            currentRegel = buildExtendedText(currentRegel, line)
                        } else {
                            val last = currentKriterien.removeLast()
                            currentKriterien += buildExtendedText(last, line)
                        }
                    }
                }
            }
            index++
        }

        flush()
        return result
    }

    private fun splitRuleAndCriteria(text: String): Pair<String, List<String>> {
        val cleaned = cleanRequirementText(text)
        if (cleaned.isBlank()) return "" to emptyList()

        val matches = requirementSplitRegex.findAll(cleaned).toList()
        if (matches.isEmpty()) {
            return cleaned to emptyList()
        }

        val criteria = mutableListOf<String>()
        var prefix = cleaned
        matches.forEachIndexed { index, match ->
            val start = match.range.last + 1
            val nextStart = matches.getOrNull(index + 1)?.range?.first ?: cleaned.length
            val content = cleaned.substring(start, nextStart).trim()
            if (index == 0) {
                val prefixText = cleaned.substring(0, match.range.first).trim()
                if (prefixText.isNotEmpty()) {
                    prefix = prefixText
                } else {
                    prefix = ""
                }
            }
            if (content.isNotEmpty()) {
                criteria += content
            }
        }
        return prefix to criteria
    }

    private fun sanitizedLines(text: String): Sequence<String> {
        return text
            .replace("\u00A0", " ")
            .lineSequence()
            .map { sanitizeLine(it) }
            .map { it.replace("|", " ").trim() }
            .filter { it.isNotEmpty() }
    }

    private fun sanitizeLine(input: String): String {
        var line = input
        footerMarkers.forEach { marker ->
            val idx = line.indexOf(marker)
            if (idx >= 0) {
                line = line.substring(0, idx)
            }
        }
        return line.trim()
    }

    private fun firstToken(line: String): String? {
        if (line.isBlank()) return null
        val token =
            line
                .takeWhile { !it.isWhitespace() }
                .trimEnd(':')
        return token.ifEmpty { null }
    }

    private fun isBlockToken(token: String): Boolean {
        val normalized = token.trim()
        if (normalized.length < 2) return false
        val stripped = normalized.replace("&", "")
        val digitIndex = stripped.indexOfFirst { it.isDigit() }
        if (digitIndex <= 0) return false
        if (!stripped.substring(digitIndex).all { it.isDigit() }) return false
        return blockTokenRegex.matches(stripped)
    }

    private fun buildExtendedText(
        base: String,
        addition: String,
    ): String =
        listOf(base, addition)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" ")
            .trim()

    private fun cleanRequirementText(text: String): String = text.replace(Regex("\\s+"), " ").trim()
}
