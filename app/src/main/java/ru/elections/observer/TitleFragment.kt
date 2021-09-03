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

        return binding.root
    }
}