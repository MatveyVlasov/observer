package ru.elections.observer.turnout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.hyy.highlightpro.HighlightPro
import com.hyy.highlightpro.parameter.Constraints
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.ElectionViewModelFactory
import ru.elections.observer.R
import ru.elections.observer.database.Election
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentTurnoutBinding
import ru.elections.observer.databinding.LayoutGuideBinding
import ru.elections.observer.utils.ID_START_TURNOUT
import ru.elections.observer.utils.Training
import ru.elections.observer.utils.getHint


class TurnoutFragment : Fragment() {
    lateinit var binding: FragmentTurnoutBinding
    lateinit var viewModel: ElectionViewModel
    lateinit var guideView: LayoutGuideBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_turnout, container, false)
        guideView = DataBindingUtil.inflate(inflater,
            R.layout.layout_guide, container, false)

        val application = requireNotNull(this.activity).application
        val database = ElectionDatabase.getInstance(application).electionDatabaseDao
        val viewModelFactory = ElectionViewModelFactory(database)
        viewModel = ViewModelProvider(
            requireActivity(),
            viewModelFactory
        ).get(ElectionViewModel::class.java)

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

        viewModel.trainingStatus.observe(viewLifecycleOwner, {
            if (it == Training.FIRST) showHints()
        })

        viewModel.guideText.observe(viewLifecycleOwner, {
            guideView.tvTips.text = it
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

    private fun showHints() {
        HighlightPro.with(this)
            .setHighlightParameter {
                getHint(guideView.root, R.id.turnout_records,
                    verticalConstraints = Constraints.BottomToTopOfHighlight)
            }
            .setHighlightParameter {
                getHint(guideView.root, R.id.official_icon_edit,
                    verticalConstraints = Constraints.BottomToTopOfHighlight,
                    horizontalConstraints = Constraints.EndToEndOfHighlight)
            }
            .setHighlightParameter { getHint(guideView.root, android.R.id.home) }
            .setBackgroundColor(R.color.black)
            .setOnShowCallback {
                viewModel.guideTextChanged(ID_START_TURNOUT + it)
            }
            .show()
    }
}

