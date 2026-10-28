package com.example.journal.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Main Room database instance for the Wayfarer application.
 * Contains the [JournalEntry] entity and provides access to [JournalEntryDao].
 */
@Database(entities = [JournalEntry::class], version = 1, exportSchema = false)
abstract class JournalDatabase : RoomDatabase() {

    /**
     * Provides access to the [JournalEntryDao] interface for CRUD operations.
     */
    abstract fun journalEntryDao(): JournalEntryDao

    companion object {
        @Volatile
        private var INSTANCE: JournalDatabase? = null

        /**
         * Returns the thread-safe singleton instance of [JournalDatabase].
         *
         * @param context Application context used to build or retrieve the database instance.
         * @return The singleton [JournalDatabase] instance.
         */
        fun getInstance(context: Context): JournalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JournalDatabase::class.java,
                    "wayfarer_journal_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
