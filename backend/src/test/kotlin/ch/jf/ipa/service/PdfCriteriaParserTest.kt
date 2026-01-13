package ch.jf.ipa.service

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PdfCriteriaParserTest {
    private val parser = PdfCriteriaParser()

    @Test
    fun `parseText extracts metadata dates and criteria`() {
        val content = """
            # Rest-API erweitern
            Kandidat/in: Rutschmann Testkandidat
            28.01.2026 – 06.02.2026
            ## Detaillierte Aufgabenstellung
            API erweitern und Archivierung

            A01 A01: Auftragsanalyse und Wahl einer Projektmethode
            Wie erfolgt die Auftragsanalyse? Welche Projektmethode kommt zum Einsatz?
            Gütestufe 3 Alle Punkte sind erfüllt. 1. Der Projektauftrag wurde analysiert. 2. Die Ziele wurden verfolgt.
            Gütestufe 2 Drei Punkte sind erfüllt.
            Gütestufe 1 Zwei Punkte sind erfüllt.
            Gütestufe 0 Weniger als zwei Punkte sind erfüllt.
        """.trimIndent()

        val dataset = parser.parseText(content)

        assertEquals("Rest-API erweitern", dataset.ipaName)
        assertEquals("API erweitern und Archivierung", dataset.topic)

        val candidate = assertNotNull(dataset.candidate)
        assertEquals("Rutschmann Testkandidat", candidate.fullName)
        assertEquals("Testkandidat", candidate.firstName)
        assertEquals("Rutschmann", candidate.lastName)

        assertEquals("2026-01-28", dataset.startDate)
        assertEquals("2026-02-06", dataset.endDate)

        assertEquals(1, dataset.criteria.size)
        val criterion = dataset.criteria.first()
        assertEquals("A01", criterion.id)
        assertEquals("Wie erfolgt die Auftragsanalyse? Welche Projektmethode kommt zum Einsatz?", criterion.title)
        assertEquals("Auftragsanalyse und Wahl einer Projektmethode", criterion.question)

        assertTrue(criterion.requirements.isNotEmpty())
        val firstRequirement = criterion.requirements.first()
        assertEquals("A01-1", firstRequirement.id)
        assertEquals("Gütestufe 3", firstRequirement.module)
        assertTrue(firstRequirement.description.startsWith("1."))
        assertTrue(firstRequirement.description.contains("Der Projektauftrag wurde analysiert."))
    }
}


