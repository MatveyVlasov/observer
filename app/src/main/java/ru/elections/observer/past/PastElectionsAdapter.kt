package ru.elections.observer.past

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.elections.observer.database.Election
import ru.elections.observer.databinding.ItemElectionBinding
import ru.elections.observer.main.MainFragmentDirections

class PastElectionsAdapter(val fragment: PastFragment) : ListAdapter<Election, PastElectionsAdapter.ViewHolder>(PastElectionsDiffCallback()) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, fragment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(private val binding: ItemElectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Election, fragment: PastFragment) {
            binding.election = item
            binding.executePendingBindings()

            binding.electionInfo.setOnClickListener {
                // fragment.findNavController().navigate()
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