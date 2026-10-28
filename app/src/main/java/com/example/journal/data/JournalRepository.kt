package com.example.journal.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface abstracting data operations for travel journal entries.
 */
interface JournalRepository {

    /**
     * Observes all journal entries sorted by date in descending order.
     *
     * @return [Flow] emitting the list of all journal entries.
     */
    fun getAllEntries(): Flow<List<JournalEntry>>

    /**
     * Observes a single journal entry by its unique identifier.
     *
     * @param id The unique ID of the entry.
     * @return [Flow] emitting the journal entry or `null` if it does not exist.
     */
    fun getEntryById(id: Long): Flow<JournalEntry?>

    /**
     * Searches journal entries by matching query string against title, location, or content.
     *
     * @param query Search query string.
     * @return [Flow] emitting matching journal entries.
     */
    fun searchEntries(query: String): Flow<List<JournalEntry>>

    /**
     * Inserts a new journal entry into persistent storage.
     *
     * @param entry The entry to insert.
     * @return The auto-generated row ID of the inserted entry.
     */
    suspend fun insertEntry(entry: JournalEntry): Long

    /**
     * Updates an existing journal entry in persistent storage.
     *
     * @param entry The entry with updated field values.
     */
    suspend fun updateEntry(entry: JournalEntry)

    /**
     * Deletes a journal entry from persistent storage.
     *
     * @param entry The entry to delete.
     */
    suspend fun deleteEntry(entry: JournalEntry)

    /**
     * Deletes a journal entry by its unique ID.
     *
     * @param id The unique identifier of the entry to delete.
     */
    suspend fun deleteEntryById(id: Long)
}

/**
 * Default implementation of [JournalRepository] backed by [JournalEntryDao].
 *
 * @property dao The [JournalEntryDao] used for database interactions.
 */
class JournalRepositoryImpl(
    private val dao: JournalEntryDao
) : JournalRepository {

    override fun getAllEntries(): Flow<List<JournalEntry>> = dao.getAllEntries()

    override fun getEntryById(id: Long): Flow<JournalEntry?> = dao.getEntryById(id)

    override fun searchEntries(query: String): Flow<List<JournalEntry>> = dao.searchEntries(query)

    override suspend fun insertEntry(entry: JournalEntry): Long = dao.insertEntry(entry)

    override suspend fun updateEntry(entry: JournalEntry) = dao.updateEntry(entry)

    override suspend fun deleteEntry(entry: JournalEntry) = dao.deleteEntry(entry)

    override suspend fun deleteEntryById(id: Long) = dao.deleteEntryById(id)
}
