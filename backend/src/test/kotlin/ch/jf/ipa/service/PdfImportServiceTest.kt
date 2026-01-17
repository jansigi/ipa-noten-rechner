package ch.jf.ipa.service

import ch.jf.ipa.dto.CandidateDto
import ch.jf.ipa.dto.CriterionDto
import ch.jf.ipa.dto.IpaDatasetDto
import ch.jf.ipa.dto.RequirementDto
import ch.jf.ipa.model.IpaDataset
import ch.jf.ipa.model.Person
import ch.jf.ipa.repository.IpaRepository
import ch.jf.ipa.repository.MetadataRepository
import ch.jf.ipa.repository.PersonRepository
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class PdfImportServiceTest {
    @Test
    fun `importIpa stores dataset and creates a new person`() =
        runTest {
            val dataset =
                IpaDatasetDto(
                    ipaName = "Rest-API erweitern",
                    topic = "API erweitern und Archivierung",
                    candidate =
                        CandidateDto(
                            fullName = "Rutschmann Testkandidat",
                            firstName = "Testkandidat",
                            lastName = "Rutschmann",
                        ),
                    startDate = "2026-01-28",
                    endDate = "2026-02-06",
                    criteria =
                        listOf(
                            CriterionDto(
                                id = "A01",
                                title = "Wie erfolgt die Auftragsanalyse?",
                                question = "A01: Auftragsanalyse",
                                requirements =
                                    listOf(
                                        RequirementDto(
                                            id = "A01-1",
                                            description = "1. Beispielkriterium",
                                            module = "Gütestufe 3",
                                            part = 1,
                                        ),
                                    ),
                            ),
                        ),
                )

            val parser =
                object : PdfCriteriaParser() {
                    override fun parse(input: InputStream): IpaDatasetDto = dataset
                }

            val metadataService = MetadataService(InMemoryMetadataRepository())
            val ipaRepository = InMemoryIpaRepository()
            val personRepository = InMemoryPersonRepository()

            val service = PdfImportService(parser, metadataService, ipaRepository, personRepository)

            val response = service.importIpa(ByteArrayInputStream(byteArrayOf(1)))

            val savedPerson = personRepository.persons.single()
            assertEquals(response.personId, savedPerson.id.toString())
            assertEquals("Testkandidat", savedPerson.firstName)
            assertEquals("Rutschmann", savedPerson.lastName)
            assertEquals("API erweitern und Archivierung", savedPerson.topic)
            assertEquals(LocalDate.parse("2026-02-06"), savedPerson.submissionDate)

            val storedDataset = ipaRepository.lastStored
            assertNotNull(storedDataset)
            assertEquals(response.datasetId, storedDataset.id.toString())
            assertEquals(savedPerson.id, storedDataset.personId)
            assertEquals(dataset.ipaName, storedDataset.ipaName)
            assertEquals(dataset.topic, storedDataset.topic)

            val storedJson = assertNotNull(ipaRepository.lastRawJson)
            val encoder = Json { ignoreUnknownKeys = true }
            assertEquals(encoder.encodeToString(dataset), storedJson)

            val metadata = metadataService.getMetadata()
            assertEquals(dataset.ipaName, metadata.ipaName)
            assertEquals(dataset.topic, metadata.topic)
            assertEquals(dataset.candidate?.fullName, metadata.candidateFullName)
            assertEquals(dataset.candidate?.firstName, metadata.candidateFirstName)
            assertEquals(dataset.candidate?.lastName, metadata.candidateLastName)
            assertEquals(response.datasetId, metadata.activeDatasetId)

            assertTrue(personRepository.clearCalled.not())
        }

    private class InMemoryIpaRepository : IpaRepository {
        var lastStored: IpaDataset? = null
        var lastRawJson: String? = null
        private val encoder =
            Json {
                ignoreUnknownKeys = true
            }

        override suspend fun createForPerson(
            personId: UUID,
            dataset: IpaDatasetDto,
        ): IpaDataset {
            val stored =
                IpaDataset(
                    id = UUID.randomUUID(),
                    personId = personId,
                    ipaName = dataset.ipaName,
                    topic = dataset.topic,
                    candidateFullName = dataset.candidate?.fullName,
                    candidateFirstName = dataset.candidate?.firstName,
                    candidateLastName = dataset.candidate?.lastName,
                    rawJson = encoder.encodeToString(dataset),
                    createdAt = Instant.now(),
                )
            lastStored = stored
            lastRawJson = stored.rawJson
            return stored
        }

        override suspend fun getForPerson(personId: UUID): IpaDataset? = lastStored?.takeIf { it.personId == personId }

        override suspend fun getLatest(): IpaDataset? = lastStored

        override suspend fun clearAll() {
            lastStored = null
            lastRawJson = null
        }
    }

    private class InMemoryMetadataRepository : MetadataRepository {
        private val values = mutableMapOf<String, String?>()

        override suspend fun getValue(key: String): String? = values[key]

        override suspend fun setValue(
            key: String,
            value: String?,
        ) {
            if (value == null) {
                values.remove(key)
            } else {
                values[key] = value
            }
        }
    }

    private class InMemoryPersonRepository : PersonRepository {
        val persons = mutableListOf<Person>()
        var clearCalled = false

        override suspend fun create(person: Person): Person {
            persons += person
            return person
        }

        override suspend fun getAll(): List<Person> = persons.toList()

        override suspend fun getById(id: UUID): Person? = persons.firstOrNull { it.id == id }

        override suspend fun clearAll() {
            clearCalled = true
            persons.clear()
        }
    }
}
