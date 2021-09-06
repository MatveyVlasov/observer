package ru.elections.observer

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.elections.observer.database.Action
import ru.elections.observer.databinding.ItemActionBinding

class LastActionsAdapter : ListAdapter<Action, LastActionsAdapter.ViewHolder>(LastActionsDiffCallback()) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(private val binding: ItemActionBinding) : RecyclerView.ViewHolder(binding.root) {
       fun bind(item: Action) {
            binding.action = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemActionBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }
}

class LastActionsDiffCallback : DiffUtil.ItemCallback<Action>() {
    override fun areItemsTheSame(oldItem: Action, newItem: Action): Boolean {
        return oldItem.actionId == newItem.actionId
    }

    override fun areContentsTheSame(oldItem: Action, newItem: Action): Boolean {
        return oldItem == newItem
    }
}