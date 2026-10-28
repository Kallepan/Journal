package com.example.journal.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity representing a single travel journal entry stored in the local Room database.
 *
 * @property id Unique auto-generated primary key identifier for the journal entry.
 * @property title The title or headline of the travel entry.
 * @property content The narrative story or content describing the travel experience.
 * @property location The geographic location or place name where the entry occurred.
 * @property timestamp Epoch time in milliseconds representing the date of the journal entry.
 * @property imageUri Optional URI string pointing to a local cover photo or image for the entry.
 * @property moodRating Subjective rating scale (1 to 5) indicating the user's mood for this memory.
 */
@Entity(tableName = "journal_entries")
data class JournalEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val title: String,
    val content: String,
    val location: String,
    val timestamp: Long,
    val imageUri: String? = null,
    val moodRating: Int
)
