package ru.elections.observer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.elections.observer.databinding.FragmentMainBinding

class MainFragment : Fragment() {
    private lateinit var binding: FragmentMainBinding

    private lateinit var viewModel: ElectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_main, container, false)

        viewModel = ViewModelProvider(this).get(ElectionViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner

//        binding.editPollingStation.doAfterTextChanged {
//            binding.editPollingStation.visibility = View.GONE
//            binding.textPollingStation.visibility = View.VISIBLE
//            binding.textPollingStation.text = it ?: "-"
//        }

        binding.editPollingStation.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.apply {
                    editPollingStation.visibility = View.GONE
                    textPollingStation.visibility = View.VISIBLE
                    textPollingStation.text = editPollingStation.text
                }
            }
            false
        }

        return binding.root
    }

}