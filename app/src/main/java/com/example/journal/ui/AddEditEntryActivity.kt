package com.example.journal.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.journal.R
import com.example.journal.data.JournalDatabase
import com.example.journal.data.JournalEntry
import com.example.journal.data.JournalRepositoryImpl
import com.example.journal.databinding.ActivityAddEditEntryBinding
import com.example.journal.domain.EntryValidator
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Activity for creating a new [JournalEntry] or editing an existing entry.
 */
class AddEditEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditEntryBinding

    private val viewModel: JournalViewModel by viewModels {
        val database = JournalDatabase.getInstance(applicationContext)
        val repository = JournalRepositoryImpl(database.journalEntryDao())
        JournalViewModelFactory(repository)
    }

    private var entryId: Long = -1L
    private var isEditMode: Boolean = false
    private var selectedTimestamp: Long = System.currentTimeMillis()
    private var selectedImageUriString: String? = null

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    // Activity Result Launcher for modern Photo Picker (PickVisualMedia)
    private val pickMediaLauncher = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val copiedUri = copyImageToInternalStorage(uri)
            selectedImageUriString = copiedUri.toString()
            binding.ivPreview.setImageURI(copiedUri)
            binding.btnSelectPhoto.text = getString(R.string.action_change_photo)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.appBarLayout.applyStatusBarInsets()

        entryId = intent.getLongExtra(EXTRA_ENTRY_ID, -1L)
        isEditMode = entryId != -1L

        setupUI()
        if (isEditMode) {
            loadExistingEntry()
        } else {
            updateDateDisplay(selectedTimestamp)
        }
    }

    private fun setupUI() {
        binding.toolbar.title = if (isEditMode) {
            getString(R.string.title_edit_entry)
        } else {
            getString(R.string.title_add_entry)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        binding.btnSelectPhoto.setOnClickListener {
            pickMediaLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
        }

        binding.etDate.setOnClickListener {
            showDatePicker()
        }

        binding.sliderMood.addOnChangeListener { _, value, _ ->
            val moodInt = value.toInt()
            binding.tvMoodValue.text = getString(R.string.mood_format, moodInt)
        }

        binding.btnSave.setOnClickListener {
            saveEntry()
        }
    }

    private fun showDatePicker() {
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select Entry Date")
            .setSelection(selectedTimestamp)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            if (selection != null) {
                selectedTimestamp = selection
                updateDateDisplay(selection)
            }
        }

        datePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
    }

    private fun updateDateDisplay(timestamp: Long) {
        binding.etDate.setText(dateFormatter.format(Date(timestamp)))
    }

    private fun loadExistingEntry() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getEntryById(entryId).collect { entry ->
                    if (entry == null) return@collect

                    binding.etTitle.setText(entry.title)
                    binding.etLocation.setText(entry.location)
                    binding.etContent.setText(entry.content)

                    selectedTimestamp = entry.timestamp
                    updateDateDisplay(selectedTimestamp)

                    binding.sliderMood.value = entry.moodRating.toFloat()
                    binding.tvMoodValue.text = getString(R.string.mood_format, entry.moodRating)

                    selectedImageUriString = entry.imageUri
                    if (!entry.imageUri.isNullOrEmpty()) {
                        try {
                            val uri = Uri.parse(entry.imageUri)
                            binding.ivPreview.setImageURI(uri)
                            binding.btnSelectPhoto.text = getString(R.string.action_change_photo)
                        } catch (_: Exception) {
                            binding.ivPreview.setImageResource(R.drawable.ic_journal_placeholder)
                        }
                    }
                }
            }
        }
    }

    private fun saveEntry() {
        val title = binding.etTitle.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()
        val content = binding.etContent.text.toString().trim()
        val moodRating = binding.sliderMood.value.toInt()

        // Clear previous errors
        binding.tilTitle.error = null
        binding.tilLocation.error = null
        binding.tilContent.error = null
        binding.tilDate.error = null

        val validationResult = EntryValidator.validate(
            title = title,
            location = location,
            content = content,
            moodRating = moodRating,
            timestamp = selectedTimestamp
        )

        if (!validationResult.isValid) {
            binding.tilTitle.error = validationResult.titleError
            binding.tilLocation.error = validationResult.locationError
            binding.tilContent.error = validationResult.contentError
            binding.tilDate.error = validationResult.dateError
            return
        }

        val entry = JournalEntry(
            id = if (isEditMode) entryId else 0L,
            title = title,
            location = location,
            content = content,
            moodRating = moodRating,
            timestamp = selectedTimestamp,
            imageUri = selectedImageUriString
        )

        if (isEditMode) {
            viewModel.updateEntry(entry) {
                Toast.makeText(this, "Journal entry updated", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            viewModel.insertEntry(entry) {
                Toast.makeText(this, "Journal entry saved", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun copyImageToInternalStorage(uri: Uri): Uri {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return uri
            val fileName = "journal_img_${UUID.randomUUID()}.jpg"
            val file = File(filesDir, fileName)
            val outputStream = FileOutputStream(file)

            inputStream.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(file)
        } catch (_: Exception) {
            uri
        }
    }

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
    }
}
