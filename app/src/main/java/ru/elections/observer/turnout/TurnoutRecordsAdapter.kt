package ru.elections.observer.turnout

import android.app.AlertDialog
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.R
import ru.elections.observer.database.Action
import ru.elections.observer.database.Election
import ru.elections.observer.databinding.ItemRecordBinding
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.database.ElectionDatabaseDao

class TurnoutRecordsAdapter(private val viewModel: ElectionViewModel, val view: View) : ListAdapter<Action, TurnoutRecordsAdapter.ViewHolder>(LastActionsDiffCallback()) {
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
                val context = view.context
                val inputEditTextField = EditText(context)
                inputEditTextField.apply {
                    inputType = InputType.TYPE_CLASS_NUMBER
                    filters += InputFilter.LengthFilter(5)
                }
                val dialog = AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.edit_official))
                    .setMessage(context.getString(R.string.edit_official_text))
                    .setView(inputEditTextField)
                    .setPositiveButton(context.getString(R.string.ok)) { _, _ ->
                        val text = inputEditTextField.text.toString()
                        if (text.isNotEmpty() && text.toInt() > 0) viewModel.onOfficialChanged(item.actionId, text.toInt())
                    }
                    .setNegativeButton(context.getString(R.string.cancel), null)
                    .create()
                dialog.show()
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemRecordBinding.inflate(layoutInflater, parent, false)
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