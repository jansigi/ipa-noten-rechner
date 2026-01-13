package ch.jf.ipa.service

import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.dto.IpaImportResponseDto
import ch.jf.ipa.model.Person
import ch.jf.ipa.repository.IpaRepository
import ch.jf.ipa.repository.PersonRepository
import java.io.InputStream
import java.time.LocalDate
import java.util.UUID

class PdfImportService(
    private val parser: PdfCriteriaParser,
    private val metadataService: MetadataService,
    private val ipaRepository: IpaRepository,
    private val personRepository: PersonRepository,
) {
    suspend fun importIpa(inputStream: InputStream): IpaImportResponseDto {
        val dataset = parser.parse(inputStream)
        if (dataset.criteria.isEmpty()) {
            throw IllegalArgumentException("Keine Kriterien im PDF gefunden.")
        }

        val person = createPersonForDataset(dataset)
        val savedDataset = ipaRepository.createForPerson(person.id, dataset)
        metadataService.setActiveDataset(savedDataset)

        return IpaImportResponseDto(
            datasetId = savedDataset.id.toString(),
            personId = person.id.toString(),
            ipaName = savedDataset.ipaName,
            topic = savedDataset.topic,
            candidateFullName = savedDataset.candidateFullName,
            criteriaCount = dataset.criteria.size,
        )
    }

    private suspend fun createPersonForDataset(dataset: IpaDatasetDto): Person {
        val (firstName, lastName) = resolveCandidateName(dataset)
        val topic = dataset.topic?.takeIf { it.isNotBlank() }
            ?: dataset.ipaName?.takeIf { it.isNotBlank() }
            ?: "IPA"

        val submissionDate = parseSubmissionDate(dataset)

        return personRepository.create(
            Person(
                id = UUID.randomUUID(),
                firstName = firstName,
                lastName = lastName,
                topic = topic,
                submissionDate = submissionDate,
            ),
        )
    }

    private fun parseSubmissionDate(dataset: IpaDatasetDto): LocalDate {
        val endDate = dataset.endDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (endDate != null) return endDate

        val startDate = dataset.startDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
        if (startDate != null) return startDate

        return LocalDate.now()
    }

    private fun resolveCandidateName(dataset: IpaDatasetDto): Pair<String, String> {
        val candidate = dataset.candidate
        val first = candidate?.firstName?.takeIf { it.isNotBlank() }
        val last = candidate?.lastName?.takeIf { it.isNotBlank() }
        if (first != null && last != null) {
            return first to last
        }

        val parts = candidate?.fullName
            ?.split(" ")
            ?.mapNotNull { it.trim().takeIf(String::isNotEmpty) }
            .orEmpty()

        if (parts.isNotEmpty()) {
            val resolvedFirst = first ?: parts.last()
            val resolvedLast = last ?: if (parts.size > 1) parts.dropLast(1).joinToString(" ") else "Kandidat"
            return resolvedFirst to resolvedLast
        }

        return "Unbekannt" to "Kandidat"
    }
}
