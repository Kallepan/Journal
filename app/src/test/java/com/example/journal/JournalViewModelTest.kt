package com.example.journal

import app.cash.turbine.test
import com.example.journal.data.JournalEntry
import com.example.journal.data.JournalRepository
import com.example.journal.ui.EntriesUiState
import com.example.journal.ui.JournalViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit test suite for [JournalViewModel] testing StateFlow emissions and repository interactions.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class JournalViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: JournalRepository = mockk(relaxed = true)
    private lateinit var viewModel: JournalViewModel

    private val sampleEntry = JournalEntry(
        id = 1L,
        title = "Tokyo Ramen Tour",
        content = "Tried 5 different ramen shops in Shinjuku.",
        location = "Tokyo, Japan",
        timestamp = 1700000000000L,
        imageUri = "content://media/external/images/1",
        moodRating = 5
    )

    @Before
    fun setUp() {
        every { repository.getAllEntries() } returns flowOf(emptyList())
        viewModel = JournalViewModel(repository)
    }

    @Test
    fun `entriesUiState emits Empty when repository returns empty list`() = runTest {
        every { repository.getAllEntries() } returns flowOf(emptyList())
        viewModel = JournalViewModel(repository)

        viewModel.entriesUiState.test {
            val state = awaitItem()
            assertTrue(state is EntriesUiState.Empty)
        }
    }

    @Test
    fun `entriesUiState emits Success when repository returns list of entries`() = runTest {
        every { repository.getAllEntries() } returns flowOf(listOf(sampleEntry))
        viewModel = JournalViewModel(repository)

        viewModel.entriesUiState.test {
            val state = awaitItem()
            assertTrue(state is EntriesUiState.Success)
            assertEquals(listOf(sampleEntry), (state as EntriesUiState.Success).entries)
        }
    }

    @Test
    fun `searchQuery filter triggers repository searchEntries query`() = runTest {
        every { repository.searchEntries("Tokyo") } returns flowOf(listOf(sampleEntry))

        viewModel.setSearchQuery("Tokyo")

        viewModel.entriesUiState.test {
            val state = awaitItem()
            assertTrue(state is EntriesUiState.Success)
            assertEquals(listOf(sampleEntry), (state as EntriesUiState.Success).entries)
        }
    }

    @Test
    fun `insertEntry invokes repository insertEntry`() = runTest {
        coEvery { repository.insertEntry(sampleEntry) } returns 1L

        var insertedId = 0L
        viewModel.insertEntry(sampleEntry) { id ->
            insertedId = id
        }

        coVerify { repository.insertEntry(sampleEntry) }
        assertEquals(1L, insertedId)
    }

    @Test
    fun `updateEntry invokes repository updateEntry`() = runTest {
        var completed = false
        viewModel.updateEntry(sampleEntry) {
            completed = true
        }

        coVerify { repository.updateEntry(sampleEntry) }
        assertTrue(completed)
    }

    @Test
    fun `deleteEntry invokes repository deleteEntry`() = runTest {
        var completed = false
        viewModel.deleteEntry(sampleEntry) {
            completed = true
        }

        coVerify { repository.deleteEntry(sampleEntry) }
        assertTrue(completed)
    }
}
