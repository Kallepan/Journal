package com.example.journal.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.journal.R
import com.example.journal.data.JournalDatabase
import com.example.journal.data.JournalEntry
import com.example.journal.data.JournalRepositoryImpl
import com.example.journal.databinding.ActivityDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Activity for displaying complete details of a single [JournalEntry].
 * Provides options to edit or delete the entry.
 */
class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding

    private val viewModel: JournalViewModel by viewModels {
        val database = JournalDatabase.getInstance(applicationContext)
        val repository = JournalRepositoryImpl(database.journalEntryDao())
        JournalViewModelFactory(repository)
    }

    private var currentEntry: JournalEntry? = null
    private var entryId: Long = -1L
    private val dateFormatter = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appBarLayout.applyStatusBarInsets()

        entryId = intent.getLongExtra(EXTRA_ENTRY_ID, -1L)
        if (entryId == -1L) {
            Toast.makeText(this, "Entry not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        observeEntry()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            handleMenuItemClick(menuItem)
        }
    }

    private fun handleMenuItemClick(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                currentEntry?.let { entry ->
                    val intent = Intent(this, AddEditEntryActivity::class.java).apply {
                        putExtra(AddEditEntryActivity.EXTRA_ENTRY_ID, entry.id)
                    }
                    startActivity(intent)
                }
                true
            }
            R.id.action_delete -> {
                confirmDelete()
                true
            }
            else -> false
        }
    }

    private fun observeEntry() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getEntryById(entryId).collect { entry ->
                    if (entry == null) {
                        // Entry was deleted or not found
                        return@collect
                    }
                    currentEntry = entry
                    bindEntryDetails(entry)
                }
            }
        }
    }

    private fun bindEntryDetails(entry: JournalEntry) {
        binding.tvDetailTitle.text = entry.title
        binding.tvDetailDate.text = dateFormatter.format(Date(entry.timestamp))
        binding.chipDetailLocation.text = entry.location
        binding.chipDetailMood.text = getString(R.string.mood_format, entry.moodRating)
        binding.tvDetailContent.text = entry.content

        if (!entry.imageUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(entry.imageUri)
                binding.ivHeaderPhoto.setImageURI(uri)
                binding.ivHeaderPhoto.visibility = View.VISIBLE
            } catch (_: Exception) {
                binding.ivHeaderPhoto.setImageResource(R.drawable.ic_journal_placeholder)
            }
        } else {
            binding.ivHeaderPhoto.setImageResource(R.drawable.ic_journal_placeholder)
        }
    }

    private fun confirmDelete() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.confirm_delete_title)
            .setMessage(R.string.confirm_delete_message)
            .setPositiveButton(R.string.confirm_delete_positive) { _, _ ->
                currentEntry?.let { entry ->
                    viewModel.deleteEntry(entry) {
                        Toast.makeText(this, "Entry deleted", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }
            .setNegativeButton(R.string.confirm_delete_negative, null)
            .show()
    }

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
    }
}
