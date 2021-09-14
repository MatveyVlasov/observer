package ru.elections.observer.title

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.hyy.highlightpro.HighlightPro
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.ElectionViewModelFactory
import ru.elections.observer.R
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentTitleBinding
import ru.elections.observer.databinding.LayoutGuideBinding
import ru.elections.observer.utils.GAP
import ru.elections.observer.utils.ID_START_TITLE
import ru.elections.observer.utils.Training
import ru.elections.observer.utils.getHint


class TitleFragment : Fragment() {
    lateinit var binding: FragmentTitleBinding
    lateinit var viewModel: ElectionViewModel
    lateinit var guideView: LayoutGuideBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_title, container, false)
        guideView = DataBindingUtil.inflate(inflater,
            R.layout.layout_guide, container, false)

        val application = requireNotNull(this.activity).application
        val database = ElectionDatabase.getInstance(application).electionDatabaseDao
        val viewModelFactory = ElectionViewModelFactory(database)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ElectionViewModel::class.java)

        binding.electionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.apply {
            setHomeButtonEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) { exitApp() }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.navigateToMainFragment.observe(viewLifecycleOwner, {
            if (it == true) {
                navigateToMain()
                viewModel.doneNavigatingToMain()
            }
        })

        viewModel.navigateToPastFragment.observe(viewLifecycleOwner, {
            if (it == true) {
                navigateToPast()
                viewModel.doneNavigatingToPast()
            }
        })

        viewModel.trainingStatus.observe(viewLifecycleOwner, {
            if (it != Training.NO) showHints(it)
        })

        viewModel.guideText.observe(viewLifecycleOwner, {
            guideView.tvTips.text = it
        })

        binding.buttonAbout.setOnClickListener {
            onAboutButton()
        }
    }

    private fun navigateToMain() {
        findNavController().navigate(
            TitleFragmentDirections.actionTitleFragmentToMainFragment()
        )
    }

    private fun navigateToPast() {
        findNavController().navigate(
            TitleFragmentDirections.actionTitleFragmentToPastFragment()
        )
    }

    private fun showHints(trainingStatus: Training) {
        if (trainingStatus == Training.FIRST) {
            HighlightPro.with(this)
                .setHighlightParameter { getHint(guideView.root, R.id.button_new_election) }
                .setBackgroundColor(R.color.black)
                .setOnShowCallback {
                    viewModel.guideTextChanged(ID_START_TITLE + it)
                }
                .show()
        } else {
            HighlightPro.with(this)
                .setHighlightParameter { getHint(guideView.root, R.id.button_past_elections) }
                .setBackgroundColor(R.color.black)
                .setOnShowCallback {
                    viewModel.guideTextChanged(ID_START_TITLE + GAP + it)
                }
                .show()
        }
    }

    private fun onAboutButton() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.about_app))
            .setMessage(
                HtmlCompat.fromHtml(getString(R.string.about_app_text),
                    HtmlCompat.FROM_HTML_MODE_LEGACY))
            .setPositiveButton(getString(R.string.ok),null)
            .show()
    }

    private fun exitApp() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.exit))
            .setMessage(getString(R.string.exit_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> finishAffinity(requireActivity()) }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
}
