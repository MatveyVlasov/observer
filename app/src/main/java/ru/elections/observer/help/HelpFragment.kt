package ru.elections.observer.help

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
import ru.elections.observer.databinding.FragmentHelpBinding


class HelpFragment : Fragment() {
    lateinit var binding: FragmentHelpBinding
    lateinit var viewModel: ElectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_help, container, false)

        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

    }
}

