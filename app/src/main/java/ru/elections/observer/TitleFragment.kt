package ru.elections.observer

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
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

        viewModel.navigateToMainFragment.observe(viewLifecycleOwner, {
            if (it == true) {
                this.findNavController().navigate(
                    TitleFragmentDirections.actionTitleFragmentToMainFragment())
                viewModel.doneNavigating()
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.exit))
                .setMessage(getString(R.string.exit_confirmation))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> finishAffinity(requireActivity()) }
                .setNegativeButton(getString(R.string.no)) { _, _ ->  }
                .show()
        }

        return binding.root
    }
}
