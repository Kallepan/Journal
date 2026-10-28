package com.example.journal.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.journal.data.JournalEntry
import com.example.journal.data.JournalRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State representation for the entries list screen.
 */
sealed interface EntriesUiState {
    /** State representing active data loading. */
    object Loading : EntriesUiState

    /** State representing successful retrieval of journal entries. */
    data class Success(val entries: List<JournalEntry>) : EntriesUiState

    /** State representing an empty dataset (no entries match query). */
    object Empty : EntriesUiState

    /** State representing an error encountered while fetching data. */
    data class Error(val message: String) : EntriesUiState
}

/**
 * ViewModel managing UI state and business logic for journal entry operations.
 *
 * @property repository The [JournalRepository] managing database persistence.
 */
class JournalViewModel(
    private val repository: JournalRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    /** Current search query string. */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    /** StateFlow emitting the current [EntriesUiState] based on the database and search query. */
    val entriesUiState: StateFlow<EntriesUiState> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllEntries()
            } else {
                repository.searchEntries(query)
            }
        }
        .map { entries ->
            if (entries.isEmpty()) {
                EntriesUiState.Empty
            } else {
                EntriesUiState.Success(entries)
            }
        }
        .catch { throwable ->
            emit(EntriesUiState.Error(throwable.message ?: "An unexpected error occurred"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EntriesUiState.Loading
        )

    /**
     * Sets or updates the search query filter.
     *
     * @param query Search query text.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Observes a specific journal entry by its unique ID.
     *
     * @param id The unique identifier of the entry.
     * @return [StateFlow] emitting the matching [JournalEntry] or `null`.
     */
    fun getEntryById(id: Long): StateFlow<JournalEntry?> {
        return repository.getEntryById(id).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    /**
     * Inserts a new journal entry.
     *
     * @param entry The entry to insert.
     * @param onComplete Optional callback returning the inserted row ID.
     */
    fun insertEntry(entry: JournalEntry, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = repository.insertEntry(entry)
            onComplete?.invoke(id)
        }
    }

    /**
     * Updates an existing journal entry.
     *
     * @param entry The entry to update.
     * @param onComplete Optional callback triggered upon completion.
     */
    fun updateEntry(entry: JournalEntry, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.updateEntry(entry)
            onComplete?.invoke()
        }
    }

    /**
     * Deletes a journal entry.
     *
     * @param entry The entry to delete.
     * @param onComplete Optional callback triggered upon completion.
     */
    fun deleteEntry(entry: JournalEntry, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteEntry(entry)
            onComplete?.invoke()
        }
    }

    /**
     * Deletes a journal entry by its unique ID.
     *
     * @param id The unique identifier of the entry.
     * @param onComplete Optional callback triggered upon completion.
     */
    fun deleteEntryById(id: Long, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            repository.deleteEntryById(id)
            onComplete?.invoke()
        }
    }
}

/**
 * Factory for creating [JournalViewModel] instances with a [JournalRepository] dependency.
 *
 * @property repository The [JournalRepository] to inject into the ViewModel.
 */
class JournalViewModelFactory(
    private val repository: JournalRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(JournalViewModel::class.java)) {
            return JournalViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
