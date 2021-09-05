package ru.elections.observer

import android.app.AlertDialog
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentMainBinding
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import java.util.*
import kotlin.concurrent.schedule

class MainFragment : Fragment() {
    lateinit var binding: FragmentMainBinding
    lateinit var viewModel: ElectionViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(inflater,
                        R.layout.fragment_main, container, false)

        val application = requireNotNull(this.activity).application
        val database = ElectionDatabase.getInstance(application).electionDatabaseDao
        val viewModelFactory = ElectionViewModelFactory(database)
        viewModel = ViewModelProvider(requireActivity(), viewModelFactory).get(ElectionViewModel::class.java)

        binding.electionViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner


        binding.pollingStationEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.pollingStationEdit.text.toString()
                if (text.isEmpty() || text.toInt() < 0) return@setOnEditorActionListener false
                onPollingStationChosen(text.toInt())
                view?.hideKeyboard()
            }
            false
        }

        binding.totalVotersEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.totalVotersEdit.text.toString()
                if (text.isEmpty() || text.toInt() < 0) return@setOnEditorActionListener false
                onTotalVotersChosen(text.toInt())
                view?.hideKeyboard()
            }
            false
        }

        binding.votedEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.votedEdit.text.toString()
                if (text.isEmpty() || text.toInt() < 0) return@setOnEditorActionListener false
                onVotedChosen(text.toInt())
                view?.hideKeyboard()
            }
            false
        }

        viewModel.currentElection.observe(viewLifecycleOwner, {
            Log.i("MainFragment", "Check if null")
            Log.i("MainFragment", it.toString())
            if (it == null) navigateToTitle()
            else binding.apply {
                pollingStationNumber.text =
                    if (it.pollingStation == -1)  "-" else it.pollingStation.toString()
                totalVotersNumber.text =
                    if (it.totalVoters == -1)  "-" else it.totalVoters.toString()
                votedNumber.text = it.voted.toString()
                counterNumber.text = it.counter.toString()
                turnoutText.text = viewModel.getTurnout()
                Timer().schedule(100)  { lastActions.smoothScrollToPosition(0) }
            }
        })

        binding.pollingStationIconEdit.setOnClickListener {
            onPollingStationIconEditSelected()
        }

        binding.totalVotersIconEdit.setOnClickListener {
            onTotalVotersIconEditSelected()
        }

        binding.votedIconEdit.setOnClickListener {
            onVotedIconEditSelected()
        }

        viewModel.showSnackbarEvent.observe(viewLifecycleOwner, {
            if (it == true) {
                Snackbar.make(requireView(),
                    getString(R.string.negative_counter), Snackbar.LENGTH_SHORT).show()
                viewModel.doneShowingSnackbar()
            }
        })

        val adapter = LastActionsAdapter()
        binding.lastActions.adapter = adapter

        viewModel.actions.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
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

    private fun navigateToTitle() {
        Log.i("MainFragment", "Navigate to title")
        this.findNavController().navigate(MainFragmentDirections.actionMainFragmentToTitleFragment())
    }

    private fun onPollingStationChosen(station: Int) {
        binding.apply {
            pollingStationEdit.visibility = View.GONE
            pollingStationIconEdit.visibility = View.VISIBLE
            pollingStationNumber.visibility = View.VISIBLE
            viewModel.onPollingStationChanged(station)
        }
    }

    private fun onTotalVotersChosen(voters: Int) {
        binding.apply {
            totalVotersEdit.visibility = View.GONE
            totalVotersIconEdit.visibility = View.VISIBLE
            totalVotersNumber.visibility = View.VISIBLE
            //turnoutText.text = viewModel.getTurnout()
            viewModel.onTotalVotersChanged(voters)
        }
    }

    private fun onVotedChosen(voters: Int) {
        binding.apply {
            votedEdit.visibility = View.GONE
            votedIconEdit.visibility = View.VISIBLE
            votedNumber.visibility = View.VISIBLE
            viewModel.onVotedChanged(voters)
        }
    }


    private fun onPollingStationIconEditSelected() {
        binding.apply {
            pollingStationEdit.visibility = View.VISIBLE
            pollingStationIconEdit.visibility = View.GONE
            pollingStationNumber.visibility = View.GONE
            if (viewModel.currentElection.value?.pollingStation == -1) {
                pollingStationEdit.text = Editable.Factory.getInstance().newEditable("")
            }
        }
    }

    private fun onTotalVotersIconEditSelected() {
        binding.apply {
            totalVotersEdit.visibility = View.VISIBLE
            totalVotersIconEdit.visibility = View.GONE
            totalVotersNumber.visibility = View.GONE
            if (viewModel.currentElection.value?.totalVoters == -1) {
                totalVotersEdit.text = Editable.Factory.getInstance().newEditable("")
            }
        }
    }

    private fun onVotedIconEditSelected() {
        binding.apply {
            votedEdit.visibility = View.VISIBLE
            votedIconEdit.visibility = View.GONE
            votedNumber.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.finish_election -> {
                AlertDialog.Builder(context)
                    .setTitle(getString(R.string.election_finish))
                    .setMessage(getString(R.string.election_finish_confirmation))
                    .setPositiveButton(getString(R.string.yes)) { _, _ -> onFinishYes() }
                    .setNegativeButton(getString(R.string.no)) { _, _ ->  }
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onFinishYes() {
        navigateToTitle()
        viewModel.finishElection()
        Toast.makeText(context, getString(R.string.election_finished), Toast.LENGTH_LONG).show()
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}