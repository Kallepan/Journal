package com.example.journal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.journal.data.JournalDatabase
import com.example.journal.data.JournalRepositoryImpl
import com.example.journal.databinding.ActivityMainBinding
import com.example.journal.ui.AddEditEntryActivity
import com.example.journal.ui.DetailActivity
import com.example.journal.ui.EntriesUiState
import com.example.journal.ui.JournalAdapter
import com.example.journal.ui.JournalViewModel
import com.example.journal.ui.JournalViewModelFactory
import com.example.journal.ui.applyStatusBarInsets
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Main activity serving as the overview dashboard for travel journal entries.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: JournalViewModel by viewModels {
        val database = JournalDatabase.getInstance(applicationContext)
        val repository = JournalRepositoryImpl(database.journalEntryDao())
        JournalViewModelFactory(repository)
    }

    private lateinit var adapter: JournalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        applySavedTheme()
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appBarLayout.applyStatusBarInsets()

        setupToolbar()
        setupRecyclerView()
        setupFab()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.inflateMenu(R.menu.main_menu)

        val searchItem = binding.toolbar.menu.findItem(R.id.action_search)
        val searchView = searchItem?.actionView as? SearchView

        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSearchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSearchQuery(newText.orEmpty())
                return true
            }
        })

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_theme -> {
                    showThemeSelectionDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showThemeSelectionDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)

        val options = arrayOf(
            getString(R.string.theme_light),
            getString(R.string.theme_dark),
            getString(R.string.theme_system)
        )

        val selectedIndex = when (currentMode) {
            AppCompatDelegate.MODE_NIGHT_NO -> 0
            AppCompatDelegate.MODE_NIGHT_YES -> 1
            else -> 2
        }

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.dialog_theme_title)
            .setSingleChoiceItems(options, selectedIndex) { dialog, which ->
                val newMode = when (which) {
                    0 -> AppCompatDelegate.MODE_NIGHT_NO
                    1 -> AppCompatDelegate.MODE_NIGHT_YES
                    else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                }

                prefs.edit().putInt(KEY_THEME_MODE, newMode).apply()
                AppCompatDelegate.setDefaultNightMode(newMode)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.confirm_delete_negative, null)
            .show()
    }

    private fun applySavedTheme() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentMode = prefs.getInt(KEY_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(currentMode)
    }

    private fun setupRecyclerView() {
        adapter = JournalAdapter { entry ->
            val intent = Intent(this, DetailActivity::class.java).apply {
                putExtra(DetailActivity.EXTRA_ENTRY_ID, entry.id)
            }
            startActivity(intent)
        }
        binding.recyclerViewEntries.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddEntry.setOnClickListener {
            val intent = Intent(this, AddEditEntryActivity::class.java)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entriesUiState.collect { state ->
                    when (state) {
                        is EntriesUiState.Loading -> {
                            binding.recyclerViewEntries.visibility = View.GONE
                            binding.emptyStateContainer.visibility = View.GONE
                        }
                        is EntriesUiState.Success -> {
                            binding.recyclerViewEntries.visibility = View.VISIBLE
                            binding.emptyStateContainer.visibility = View.GONE
                            adapter.submitList(state.entries)
                        }
                        is EntriesUiState.Empty -> {
                            binding.recyclerViewEntries.visibility = View.GONE
                            binding.emptyStateContainer.visibility = View.VISIBLE
                            adapter.submitList(emptyList())
                        }
                        is EntriesUiState.Error -> {
                            binding.recyclerViewEntries.visibility = View.GONE
                            binding.emptyStateContainer.visibility = View.VISIBLE
                            adapter.submitList(emptyList())
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "wayfarer_theme_prefs"
        private const val KEY_THEME_MODE = "key_theme_mode"
    }
}
