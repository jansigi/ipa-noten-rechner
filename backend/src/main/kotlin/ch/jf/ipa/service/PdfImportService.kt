package ch.jf.ipa.service

import ch.jf.ipa.dto.IpaImportResponseDto
import ch.jf.ipa.repository.CriteriaRepository
import ch.jf.ipa.repository.IpaRepository
import java.io.InputStream

class PdfImportService(
    private val parser: PdfCriteriaParser,
    private val criteriaRepository: CriteriaRepository,
    private val metadataService: MetadataService,
    private val ipaRepository: IpaRepository,
) {
    suspend fun importIpa(inputStream: InputStream): IpaImportResponseDto {
        val dataset = parser.parse(inputStream)
        if (dataset.criteria.isEmpty()) {
            throw IllegalArgumentException("Keine Kriterien im PDF gefunden.")
        }

        val savedDataset = ipaRepository.replaceActiveDataset(dataset)
        metadataService.setActiveDataset(savedDataset)
        criteriaRepository.replaceAll(dataset.criteria)

        return IpaImportResponseDto(
            datasetId = savedDataset.id.toString(),
            ipaName = savedDataset.ipaName,
            topic = savedDataset.topic,
            candidateFullName = savedDataset.candidateFullName,
            criteriaCount = dataset.criteria.size,
        )
    }
}

