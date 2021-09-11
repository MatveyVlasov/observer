package ru.elections.observer.past

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.ElectionViewModelFactory
import ru.elections.observer.R
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentPastBinding


class PastFragment : Fragment() {
    lateinit var binding: FragmentPastBinding
    lateinit var viewModel: ElectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_past, container, false)

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
        val adapter = PastElectionsAdapter(viewModel, this)
        binding.pastElections.adapter = adapter


        viewModel.elections.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

    }
}

