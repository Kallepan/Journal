package com.example.journal.domain

/**
 * Result data class holding validation outcomes for journal entry input fields.
 *
 * @property isValid True if all validation rules are satisfied, false otherwise.
 * @property titleError Error message string if title is invalid, or null if valid.
 * @property locationError Error message string if location is invalid, or null if valid.
 * @property contentError Error message string if content narrative is invalid, or null if valid.
 * @property moodError Error message string if mood rating is out of bounds, or null if valid.
 * @property dateError Error message string if timestamp is invalid, or null if valid.
 */
data class ValidationResult(
    val isValid: Boolean,
    val titleError: String? = null,
    val locationError: String? = null,
    val contentError: String? = null,
    val moodError: String? = null,
    val dateError: String? = null
)

/**
 * Pure Kotlin validation utility for validating input parameters of a journal entry.
 */
object EntryValidator {

    /**
     * Validates journal entry fields against business rules.
     *
     * @param title Title of the entry. Must not be blank.
     * @param location Location name. Must not be blank.
     * @param content Narrative body content. Must not be blank.
     * @param moodRating Mood rating score. Must be between 1 and 5 (inclusive).
     * @param timestamp Timestamp in milliseconds. Must be greater than 0.
     * @return [ValidationResult] containing validation status and field-specific error messages.
     */
    fun validate(
        title: String,
        location: String,
        content: String,
        moodRating: Int,
        timestamp: Long
    ): ValidationResult {
        var isValid = true
        var titleErr: String? = null
        var locationErr: String? = null
        var contentErr: String? = null
        var moodErr: String? = null
        var dateErr: String? = null

        if (title.isBlank()) {
            isValid = false
            titleErr = "Title cannot be empty"
        }

        if (location.isBlank()) {
            isValid = false
            locationErr = "Location cannot be empty"
        }

        if (content.isBlank()) {
            isValid = false
            contentErr = "Story content cannot be empty"
        }

        if (moodRating !in 1..5) {
            isValid = false
            moodErr = "Mood rating must be between 1 and 5"
        }

        if (timestamp <= 0L) {
            isValid = false
            dateErr = "Date must be a valid timestamp"
        }

        return ValidationResult(
            isValid = isValid,
            titleError = titleErr,
            locationError = locationErr,
            contentError = contentErr,
            moodError = moodErr,
            dateError = dateErr
        )
    }
}
