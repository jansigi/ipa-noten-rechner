package ch.jf.ipa.service

import ch.jf.ipa.dto.CandidateDto
import ch.jf.ipa.dto.CriterionDto
import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.dto.RequirementDto
import java.io.InputStream
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

class PdfCriteriaParser {
    fun parse(input: InputStream): IpaDatasetDto {
        val text = extractTextFromPdf(input)
        return parseText(text)
    }

    internal fun parseText(content: String): IpaDatasetDto {
        val normalizedContent = content.replace("\r\n", "\n")
        val lines = normalizedContent.lines()

        val parsedCriteria = KriterienParser.parse(normalizedContent)
        val criteria = parsedCriteria.map { it.toCriterionDto() }
        val ipaName = extractIpaName(lines)
        val candidate = extractCandidate(lines)
        val topic = extractTopic(lines, ipaName)

        return IpaDatasetDto(
            ipaName = ipaName,
            topic = topic,
            candidate = candidate,
            criteria = criteria,
        )
    }

    private fun extractIpaName(lines: List<String>): String? {
        val heading = lines
            .map { it.trim() }
            .firstOrNull { it.startsWith("# ") }
            ?: return null
        return heading.removePrefix("#").trim()
    }

    private fun extractTopic(lines: List<String>, fallback: String?): String? {
        val topicLine = lines
            .map { it.trim() }
            .firstOrNull { it.startsWith("## Detaillierte Aufgabenstellung", ignoreCase = true) }
            ?.substringAfter("## Detaillierte Aufgabenstellung", missingDelimiterValue = "")
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        return topicLine ?: fallback
    }

    private fun extractCandidate(lines: List<String>): CandidateDto? {
        val regex = Regex("Kandidat/in\\s*:\\s*(.+)", RegexOption.IGNORE_CASE)
        val fullName = lines
            .asSequence()
            .mapNotNull { line -> regex.find(line)?.groupValues?.getOrNull(1)?.trim() }
            .firstOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: return null

        val parts = fullName.split(" ").filter { it.isNotBlank() }
        if (parts.isEmpty()) {
            return CandidateDto(fullName = fullName)
        }
        val firstName = parts.last()
        val lastName = parts.dropLast(1).joinToString(" ").takeIf { it.isNotBlank() }

        return CandidateDto(
            fullName = fullName,
            firstName = firstName,
            lastName = lastName,
        )
    }

    private fun Kriterium.toCriterionDto(): CriterionDto {
        val requirements = mutableListOf<RequirementDto>()
        var counter = 1
        guetestufen.forEach { stufe ->
            val moduleLabel = stufe.bezeichnung.ifBlank { "G${stufe.stufe}" }
            if (stufe.kriterien.isEmpty()) {
                if (stufe.regel.isNotBlank()) {
                    requirements += RequirementDto(
                        id = "$id-${counter++}",
                        description = stufe.regel,
                        module = moduleLabel,
                        part = 1,
                    )
                }
            } else {
                stufe.kriterien.forEachIndexed { index, text ->
                    val description = if (index == 0 && stufe.regel.isNotBlank()) {
                        "${stufe.regel} $text".trim()
                    } else {
                        text
                    }
                    requirements += RequirementDto(
                        id = "$id-${counter++}",
                        description = description,
                        module = moduleLabel,
                        part = index + 1,
                    )
                }
            }
        }
        return CriterionDto(
            id = id,
            title = titel,
            question = frage,
            requirements = requirements,
        )
    }

    private fun extractTextFromPdf(input: InputStream): String {
        PDDocument.load(input).use { document ->
            val stripper = PDFTextStripper()
            stripper.sortByPosition = true
            return stripper.getText(document)
        }
    }
}

