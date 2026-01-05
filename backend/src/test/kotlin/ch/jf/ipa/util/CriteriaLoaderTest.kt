package ch.jf.ipa.util

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CriteriaLoaderTest {
    @Test
    fun loadCriteriaReturnsList() {
        val criteria = CriteriaLoader.loadCriteria()
        assertNotNull(criteria)
        assertTrue { criteria.size == 100 }
    }
}

