package ru.elections.observer.past

import android.os.Bundle
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
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentPastBinding
import ru.elections.observer.databinding.LayoutGuideBinding
import ru.elections.observer.utils.ID_START_PAST
import ru.elections.observer.utils.ID_START_TURNOUT
import ru.elections.observer.utils.Training
import ru.elections.observer.utils.getHint


class PastFragment : Fragment() {
    lateinit var binding: FragmentPastBinding
    lateinit var viewModel: ElectionViewModel
    lateinit var guideView: LayoutGuideBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_past, container, false)
        guideView = DataBindingUtil.inflate(inflater,
            R.layout.layout_guide, container, false)

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
        viewModel.trainingStatus.observe(viewLifecycleOwner, {
            if (it == Training.SECOND) showHints()
        })

        viewModel.guideText.observe(viewLifecycleOwner, {
            guideView.tvTips.text = it
        })

        val adapter = PastElectionsAdapter(viewModel, this)
        binding.pastElections.adapter = adapter


        viewModel.elections.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })
    }

    private fun showHints() {
        HighlightPro.with(this)
            .setHighlightParameter {
                getHint(guideView.root, R.id.election_date,
                    horizontalConstraints = Constraints.StartToEndOfHighlight)
            }
            .setHighlightParameter {
                getHint(guideView.root, R.id.election_info,
                    horizontalConstraints = Constraints.EndToEndOfHighlight)
            }
            .setHighlightParameter {
                getHint(guideView.root, R.id.election_delete,
                    horizontalConstraints = Constraints.EndToEndOfHighlight)
            }
            .setBackgroundColor(R.color.black)
            .setOnShowCallback {
                viewModel.guideTextChanged(ID_START_PAST + it)
            }
            .show()
    }
}

