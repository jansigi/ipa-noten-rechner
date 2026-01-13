package ch.jf.ipa.service

import ch.jf.ipa.dto.CandidateDto
import ch.jf.ipa.dto.CriterionDto
import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.dto.RequirementDto
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper

private val DATE_RANGE_REGEX = Regex("(\\d{1,2}\\.\\d{1,2}\\.\\d{4})\\s*[–-]\\s*(\\d{1,2}\\.\\d{1,2}\\.\\d{4})")
private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

open class PdfCriteriaParser {
    open fun parse(input: InputStream): IpaDatasetDto {
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
        val (startDate, endDate) = extractDateRange(lines)

        return IpaDatasetDto(
            ipaName = ipaName,
            topic = topic,
            candidate = candidate,
            startDate = startDate,
            endDate = endDate,
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
        val trimmed = lines.map { it.trim() }
        val headingIndex = trimmed.indexOfFirst { it.startsWith("## Detaillierte Aufgabenstellung", ignoreCase = true) }
        if (headingIndex == -1) {
            return fallback
        }

        val heading = trimmed[headingIndex]
        val inline = heading
            .substringAfter("## Detaillierte Aufgabenstellung", missingDelimiterValue = "")
            .trim()
            .takeIf { it.isNotEmpty() }
        if (inline != null) {
            return inline
        }

        val nextLine = trimmed.getOrNull(headingIndex + 1)?.takeIf { it.isNotBlank() }
        return nextLine ?: fallback
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

    private fun extractDateRange(lines: List<String>): Pair<String?, String?> {
        val match = lines
            .asSequence()
            .mapNotNull { DATE_RANGE_REGEX.find(it) }
            .firstOrNull()
            ?: return null to null

        val start = match.groupValues.getOrNull(1)?.parseDateToIso()
        val end = match.groupValues.getOrNull(2)?.parseDateToIso()
        return start to end
    }

    private fun String.parseDateToIso(): String? =
        try {
            LocalDate.parse(this, DATE_FORMATTER).toString()
        } catch (_: DateTimeParseException) {
            null
        }

    private fun Kriterium.toCriterionDto(): CriterionDto {
        val displayTitle = frage.ifBlank { titel }.ifBlank { id }
        val secondary = titel.takeIf { it.isNotBlank() && it != displayTitle } ?: ""

        val requirements = mutableListOf<RequirementDto>()
        var counter = 1
        guetestufen.forEach { stufe ->
            val moduleLabel = stufe.bezeichnung.ifBlank { "Gütestufe ${stufe.stufe}" }
            if (stufe.kriterien.isEmpty()) {
                val content = stufe.regel.trim()
                if (content.isNotEmpty()) {
                    requirements += RequirementDto(
                        id = "$id-${counter++}",
                        description = content,
                        module = moduleLabel,
                        part = 1,
                    )
                }
            } else {
                stufe.kriterien.forEachIndexed { index, raw ->
                    val combined = listOf(
                        if (index == 0) stufe.regel else "",
                        raw,
                    )
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .joinToString(" ")
                    val numbered = "${index + 1}. ${combined.trim()}"
                    requirements += RequirementDto(
                        id = "$id-${counter++}",
                        description = numbered,
                        module = moduleLabel,
                        part = index + 1,
                    )
                }
            }
        }

        return CriterionDto(
            id = id,
            title = displayTitle,
            question = secondary,
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

