package com.example.journal.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) defining Room database queries and mutations for [JournalEntry].
 */
@Dao
interface JournalEntryDao {

    /**
     * Retrieves all journal entries ordered by timestamp in descending order (most recent first).
     *
     * @return A [Flow] emitting the list of [JournalEntry] objects upon any database update.
     */
    @Query("SELECT * FROM journal_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<JournalEntry>>

    /**
     * Retrieves a single journal entry matching the provided [id].
     *
     * @param id The unique identifier of the entry.
     * @return A [Flow] emitting the [JournalEntry] or `null` if not found.
     */
    @Query("SELECT * FROM journal_entries WHERE id = :id LIMIT 1")
    fun getEntryById(id: Long): Flow<JournalEntry?>

    /**
     * Searches journal entries by matching [query] against title, location, or narrative content.
     *
     * @param query The search query string.
     * @return A [Flow] emitting matching [JournalEntry] objects ordered by timestamp descending.
     */
    @Query("""
        SELECT * FROM journal_entries 
        WHERE title LIKE '%' || :query || '%' 
           OR location LIKE '%' || :query || '%' 
           OR content LIKE '%' || :query || '%' 
        ORDER BY timestamp DESC
    """)
    fun searchEntries(query: String): Flow<List<JournalEntry>>

    /**
     * Inserts a new journal entry into the database.
     *
     * @param entry The [JournalEntry] to insert.
     * @return The auto-generated row ID of the inserted entry.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: JournalEntry): Long

    /**
     * Updates an existing journal entry in the database.
     *
     * @param entry The [JournalEntry] containing updated values.
     */
    @Update
    suspend fun updateEntry(entry: JournalEntry)

    /**
     * Deletes a journal entry from the database.
     *
     * @param entry The [JournalEntry] to delete.
     */
    @Delete
    suspend fun deleteEntry(entry: JournalEntry)

    /**
     * Deletes a journal entry from the database by its unique [id].
     *
     * @param id The ID of the entry to delete.
     */
    @Query("DELETE FROM journal_entries WHERE id = :id")
    suspend fun deleteEntryById(id: Long)
}
