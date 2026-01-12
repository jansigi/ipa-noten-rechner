package ch.jf.ipa.service

import kotlin.test.Test
import kotlin.test.assertEquals

class GradingServiceTest {
    @Test
    fun `grade levels for six requirements`() {
        val total = 6
        assertEquals(3, GradingService.calculateGradeLevel(6, total))
        assertEquals(2, GradingService.calculateGradeLevel(5, total))
        assertEquals(2, GradingService.calculateGradeLevel(4, total))
        assertEquals(1, GradingService.calculateGradeLevel(3, total))
        assertEquals(1, GradingService.calculateGradeLevel(2, total))
        assertEquals(0, GradingService.calculateGradeLevel(1, total))
        assertEquals(0, GradingService.calculateGradeLevel(0, total))
    }

    @Test
    fun `grade levels for single requirement`() {
        val total = 1
        assertEquals(3, GradingService.calculateGradeLevel(1, total))
        assertEquals(0, GradingService.calculateGradeLevel(0, total))
    }

    @Test
    fun `grade level is zero when no requirements`() {
        assertEquals(0, GradingService.calculateGradeLevel(0, 0))
        assertEquals(0, GradingService.calculateGradeLevel(3, 0))
    }
}
