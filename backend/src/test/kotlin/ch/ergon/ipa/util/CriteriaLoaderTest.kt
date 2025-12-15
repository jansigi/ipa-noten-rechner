package ch.ergon.ipa.util

import kotlin.test.Test
import kotlin.test.assertNotNull

class CriteriaLoaderTest {
    @Test
    fun loadCriteriaReturnsList() {
        val criteria = CriteriaLoader.loadCriteria()
        assertNotNull(criteria)
    }
}

