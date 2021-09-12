package ru.elections.observer.past

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.R
import ru.elections.observer.database.Election
import ru.elections.observer.databinding.ItemElectionBinding
import ru.elections.observer.main.MainFragmentDirections
import java.util.*
import kotlin.concurrent.schedule

class PastElectionsAdapter(private val viewModel: ElectionViewModel, val fragment: PastFragment) : ListAdapter<Election, PastElectionsAdapter.ViewHolder>(PastElectionsDiffCallback()) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, viewModel, fragment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(private val binding: ItemElectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Election, viewModel: ElectionViewModel, fragment: PastFragment) {
            binding.election = item
            binding.executePendingBindings()

            binding.electionInfo.setOnClickListener {
                viewModel.onCurrentElectionChanged(item, fragment)
            }

            binding.electionDelete.setOnClickListener {
                val context = fragment.requireContext()
                val dialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.election_deleting))
                    .setMessage(context.getString(R.string.election_deleting_confirmation))
                    .setPositiveButton(context.getString(R.string.yes)) { _, _ ->
                        viewModel.onElectionDeleted(item)
                    }
                    .setNegativeButton(context.getString(R.string.no), null)
                    .create()
                dialog.show()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemElectionBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class PastElectionsDiffCallback : DiffUtil.ItemCallback<Election>() {
    override fun areItemsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem.electionId == newItem.electionId
    }

    override fun areContentsTheSame(oldItem: Election, newItem: Election): Boolean {
        return oldItem == newItem
    }
}