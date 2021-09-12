package ru.elections.observer.help

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.R
import ru.elections.observer.databinding.FragmentHelpTitleBinding


class HelpTitleFragment : Fragment() {
    lateinit var binding: FragmentHelpTitleBinding
    lateinit var viewModel: ElectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_help_title, container, false)

        val actionBar = (activity as AppCompatActivity?)!!.supportActionBar
        actionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.buttonNext.setOnClickListener {
            navigateToNextFragment()
        }
    }

    private fun navigateToNextFragment() {
        findNavController().navigate(
            HelpTitleFragmentDirections.actionHelpTitleFragmentToHelpMainFragment()
        )
    }
}

