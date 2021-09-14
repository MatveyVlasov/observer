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
import com.github.amlcurran.showcaseview.ShowcaseView
import com.github.amlcurran.showcaseview.targets.ActionViewTarget
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.ElectionViewModelFactory
import ru.elections.observer.R
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentTitleBinding


class TitleFragment : Fragment() {
    lateinit var binding: FragmentTitleBinding
    lateinit var viewModel: ElectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_title, container, false)

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

    private fun onAboutButton() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.about_app))
            .setMessage(
                HtmlCompat.fromHtml(getString(R.string.about_app_text), HtmlCompat.FROM_HTML_MODE_LEGACY))
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
