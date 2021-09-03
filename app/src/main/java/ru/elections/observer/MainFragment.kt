package ru.elections.observer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentMainBinding
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ru.elections.observer.database.Election

class MainFragment : Fragment() {
    lateinit var binding: FragmentMainBinding
    lateinit var viewModel: ElectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
                        R.layout.fragment_main, container, false)

        val application = requireNotNull(this.activity).application
        val database = ElectionDatabase.getInstance(application).electionDatabaseDao
        val viewModelFactory = ElectionViewModelFactory(database)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ElectionViewModel::class.java)

        binding.electionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        binding.pollingStationEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                onPollingStationChosen(binding.pollingStationEdit.text.toString().toInt())
            }
            false
        }

        viewModel.lastElection.observe(viewLifecycleOwner, {
            Log.i("Current value LAST", it.toString())
            Log.i("Current size LAST", viewModel.size.value.toString())
            if (it == null) navigateToTitle()
            binding.pollingStationNumber.text = (it?.pollingStation ?: "-").toString()
        })

        binding.iconEdit.setOnClickListener {
            onEditIconSelected()
        }

        return binding.root
    }

    private fun navigateToTitle() {
        this.findNavController().navigate(MainFragmentDirections.actionMainFragmentToTitleFragment())
    }

    private fun onPollingStationChosen(station: Int) {
        binding.apply {
            pollingStationEdit.visibility = View.GONE
            iconEdit.visibility = View.VISIBLE
            pollingStationNumber.visibility = View.VISIBLE
            viewModel.onPollingStationChanged(station)
        }
    }

    private fun onEditIconSelected() {
        binding.apply {
            pollingStationEdit.visibility = View.VISIBLE
            iconEdit.visibility = View.GONE
            pollingStationNumber.visibility = View.GONE
        }
    }

}