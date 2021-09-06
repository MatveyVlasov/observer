package ru.elections.observer.turnout

import android.content.Context
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.database.Action
import ru.elections.observer.database.Election
import ru.elections.observer.databinding.ItemRecordBinding
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.database.ElectionDatabaseDao

class TurnoutRecordsAdapter(val viewModel: ElectionViewModel, val view: View) : ListAdapter<Action, TurnoutRecordsAdapter.ViewHolder>(LastActionsDiffCallback()) {
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, viewModel, view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }


    class ViewHolder private constructor(private val binding: ItemRecordBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Action, viewModel: ElectionViewModel, view: View) {
            binding.action = item
            binding.election = viewModel.currentElection.value
            binding.executePendingBindings()

            binding.officialIconEdit.setOnClickListener {
                it.visibility = View.GONE
                binding.officialEdit.visibility = View.VISIBLE
                binding.officialEdit.text = Editable.Factory.getInstance().newEditable("")
            }

            binding.officialEdit.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    val text = binding.officialEdit.text.toString()
                    if (text.isEmpty() || text.toInt() < 0) return@setOnEditorActionListener false
                    binding.officialEdit.visibility = View.GONE
                    binding.officialIconEdit.visibility = View.VISIBLE
                    viewModel.onOfficialChanged(item.actionId, text.toInt())
                    view.hideKeyboard()
                }
                false
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRecordBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }

        private fun View.hideKeyboard() {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(windowToken, 0)
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