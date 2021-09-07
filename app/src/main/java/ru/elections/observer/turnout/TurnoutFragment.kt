package ru.elections.observer.turnout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.ElectionViewModelFactory
import ru.elections.observer.R
import ru.elections.observer.database.Election
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentTurnoutBinding
import ru.elections.observer.main.MainFragmentDirections


class TurnoutFragment : Fragment() {
    lateinit var binding: FragmentTurnoutBinding
    lateinit var viewModel: ElectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_turnout, container, false
        )

        val application = requireNotNull(this.activity).application
        val database = ElectionDatabase.getInstance(application).electionDatabaseDao
        val viewModelFactory = ElectionViewModelFactory(database)
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(ElectionViewModel::class.java)

        // binding.electionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

            return binding.root
        }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.currentElection.observe(viewLifecycleOwner, {
            if (viewModel.isElectionInitialized) {
                currentElectionObserver(it!!)
            }
        })


        val adapter = TurnoutRecordsAdapter(viewModel, requireView())
        binding.turnoutRecords.adapter = adapter


        viewModel.timeActions.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

    }

    private fun currentElectionObserver(election: Election) {
        binding.apply {
            election.let {
                pollingStationNumber.text =
                    if (it.pollingStation == -1) "-" else it.pollingStation.toString()
                totalVotersNumber.text =
                    if (it.totalVoters == -1) "-" else it.totalVoters.toString()
            }
        }
    }
}

