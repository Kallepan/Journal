package com.example.journal.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.journal.R
import com.example.journal.data.JournalEntry
import com.example.journal.databinding.ItemJournalEntryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * RecyclerView Adapter for displaying a list of [JournalEntry] items using [DiffUtil].
 *
 * @property onEntryClick Callback invoked when a user taps a journal entry card.
 */
class JournalAdapter(
    private val onEntryClick: (JournalEntry) -> Unit
) : ListAdapter<JournalEntry, JournalAdapter.ViewHolder>(DiffCallback) {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemJournalEntryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val entry = getItem(position)
        holder.bind(entry)
    }

    inner class ViewHolder(
        private val binding: ItemJournalEntryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(entry: JournalEntry) {
            binding.tvTitle.text = entry.title
            binding.tvDate.text = dateFormatter.format(Date(entry.timestamp))
            binding.chipLocation.text = entry.location
            binding.chipMood.text = binding.root.context.getString(R.string.mood_format, entry.moodRating)

            if (!entry.imageUri.isNullOrEmpty()) {
                try {
                    val uri = Uri.parse(entry.imageUri)
                    binding.ivThumbnail.setImageURI(uri)
                } catch (_: Exception) {
                    binding.ivThumbnail.setImageResource(R.drawable.ic_journal_placeholder)
                }
            } else {
                binding.ivThumbnail.setImageResource(R.drawable.ic_journal_placeholder)
            }

            binding.cardEntry.setOnClickListener {
                onEntryClick(entry)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<JournalEntry>() {
        override fun areItemsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: JournalEntry, newItem: JournalEntry): Boolean {
            return oldItem == newItem
        }
    }
}
