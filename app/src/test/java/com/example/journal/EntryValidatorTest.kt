package com.example.journal

import com.example.journal.domain.EntryValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite verifying boundary conditions and business rules in [EntryValidator].
 */
class EntryValidatorTest {

    @Test
    fun `validate with valid parameters returns valid result`() {
        val result = EntryValidator.validate(
            title = "Kyoto Gardens",
            location = "Kyoto, Japan",
            content = "Explored peaceful zen gardens in the rain.",
            moodRating = 5,
            timestamp = System.currentTimeMillis()
        )

        assertTrue(result.isValid)
        assertNull(result.titleError)
        assertNull(result.locationError)
        assertNull(result.contentError)
        assertNull(result.moodError)
        assertNull(result.dateError)
    }

    @Test
    fun `validate with blank title returns error`() {
        val result = EntryValidator.validate(
            title = "   ",
            location = "Paris",
            content = "Visited the Louvre.",
            moodRating = 4,
            timestamp = System.currentTimeMillis()
        )

        assertFalse(result.isValid)
        assertNotNull(result.titleError)
        assertEquals("Title cannot be empty", result.titleError)
    }

    @Test
    fun `validate with blank location returns error`() {
        val result = EntryValidator.validate(
            title = "Louvre Visit",
            location = "",
            content = "Visited the Louvre.",
            moodRating = 4,
            timestamp = System.currentTimeMillis()
        )

        assertFalse(result.isValid)
        assertNotNull(result.locationError)
        assertEquals("Location cannot be empty", result.locationError)
    }

    @Test
    fun `validate with blank content returns error`() {
        val result = EntryValidator.validate(
            title = "Louvre Visit",
            location = "Paris",
            content = "  \n  ",
            moodRating = 4,
            timestamp = System.currentTimeMillis()
        )

        assertFalse(result.isValid)
        assertNotNull(result.contentError)
        assertEquals("Story content cannot be empty", result.contentError)
    }

    @Test
    fun `validate with mood rating below 1 returns error`() {
        val result = EntryValidator.validate(
            title = "Rainy Day",
            location = "London",
            content = "Flight delayed.",
            moodRating = 0,
            timestamp = System.currentTimeMillis()
        )

        assertFalse(result.isValid)
        assertNotNull(result.moodError)
        assertEquals("Mood rating must be between 1 and 5", result.moodError)
    }

    @Test
    fun `validate with mood rating above 5 returns error`() {
        val result = EntryValidator.validate(
            title = "Epic Mountain Hike",
            location = "Alps",
            content = "Conquered the peak!",
            moodRating = 6,
            timestamp = System.currentTimeMillis()
        )

        assertFalse(result.isValid)
        assertNotNull(result.moodError)
        assertEquals("Mood rating must be between 1 and 5", result.moodError)
    }

    @Test
    fun `validate with zero or negative timestamp returns error`() {
        val result = EntryValidator.validate(
            title = "Timeline Glitch",
            location = "Unknown",
            content = "Back to the past.",
            moodRating = 3,
            timestamp = 0L
        )

        assertFalse(result.isValid)
        assertNotNull(result.dateError)
        assertEquals("Date must be a valid timestamp", result.dateError)
    }
}
