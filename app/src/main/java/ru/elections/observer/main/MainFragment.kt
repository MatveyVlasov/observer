package ru.elections.observer.main

import android.app.*
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.hyy.highlightpro.HighlightPro
import com.hyy.highlightpro.parameter.Constraints
import ru.elections.observer.*
import ru.elections.observer.database.Action
import ru.elections.observer.database.Election
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.databinding.FragmentMainBinding
import ru.elections.observer.databinding.LayoutGuideBinding
import ru.elections.observer.utils.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.schedule


class MainFragment : Fragment() {
    lateinit var binding: FragmentMainBinding
    lateinit var viewModel: ElectionViewModel
    lateinit var guideView: LayoutGuideBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setHasOptionsMenu(true)

        binding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_main, container, false)
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
        if (viewModel.isElectionFinished()) {
            binding.apply {
                pollingStationEdit.isEnabled = false
                totalVotersEdit.isEnabled = false
                votedEdit.isEnabled = false
            }
        }
        binding.pollingStationEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.pollingStationEdit.text.toString()
                if (text.isEmpty() || text.toInt() < 0) return@setOnEditorActionListener false
                onPollingStationChosen(text.toInt())
                view.hideKeyboard()
            }
            false
        }

        binding.totalVotersEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.totalVotersEdit.text.toString()
                if (text.isEmpty() || text.toInt() < 0) return@setOnEditorActionListener false
                onTotalVotersChosen(text.toInt())
                view.hideKeyboard()
            }
            false
        }

        binding.votedEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val text = binding.votedEdit.text.toString()
                if (text.isEmpty() || text.toInt() < 0) return@setOnEditorActionListener false
                onVotedConfirmation(text.toInt())
                view.hideKeyboard()
            }
            false
        }

        viewModel.currentElection.observe(viewLifecycleOwner, {
            if (viewModel.isElectionInitialized) {
                if (it == null) navigateToTitle()
                else currentElectionObserver(it)
            } else recordTurnout()
        })

        binding.pollingStationIconEdit.setOnClickListener { onPollingStationIconEditSelected() }

        binding.totalVotersIconEdit.setOnClickListener { onTotalVotersIconEditSelected() }

        binding.votedIconEdit.setOnClickListener { onVotedIconEditSelected() }

        binding.turnoutHours.setOnClickListener { navigateToTurnout() }

        viewModel.showSnackbar.observe(viewLifecycleOwner, {
            if (it == true) {
                Snackbar.make(requireView(),
                    getString(viewModel.snackbarResources), Snackbar.LENGTH_SHORT).show()
                viewModel.doneShowingSnackbar()
            }
        })

        viewModel.trainingStatus.observe(viewLifecycleOwner, {
            if (it != Training.NO) showHints(it)
        })

        viewModel.guideText.observe(viewLifecycleOwner, {
            guideView.tvTips.text = it
        })

        val adapter = LastActionsAdapter()
        binding.lastActions.adapter = adapter

        viewModel.actions.observe(viewLifecycleOwner, {
            it?.let {
                adapter.submitList(it)
            }
        })

    }

    private fun navigateToTitle() {
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToTitleFragment()
        )
    }

    private fun navigateToTurnout() {
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToTurnoutFragment()
        )
    }


    private fun currentElectionObserver(election: Election) {
        binding.apply {
            election.let {
                pollingStationNumber.text =
                    if (it.pollingStation == -1)  "-" else it.pollingStation.toString()
                totalVotersNumber.text =
                    if (it.totalVoters == -1)  "-" else it.totalVoters.toString()
                votedNumber.text = it.voted.toString()
                counterNumber.text = it.counter.toString()
                turnoutText.text = String.format(
                    getString(R.string.turnout_main_record), viewModel.getTurnout())

                Timer().schedule(100)  { lastActions.smoothScrollToPosition(0) }
            }
        }
    }

    private fun onPollingStationChosen(station: Int) {
        viewModel.onPollingStationChanged(station)
        binding.apply {
            pollingStationEdit.visibility = View.GONE
            pollingStationIconEdit.visibility = View.VISIBLE
            pollingStationNumber.visibility = View.VISIBLE
        }
    }

    private fun onTotalVotersChosen(voters: Int) {
        viewModel.onTotalVotersChanged(voters)
        binding.apply {
            totalVotersEdit.visibility = View.GONE
            totalVotersIconEdit.visibility = View.VISIBLE
            totalVotersNumber.visibility = View.VISIBLE
        }
    }

    private fun onVotedConfirmation(voters: Int) {
        val counted = viewModel.currentElection.value?.counter ?: -1
        if (counted <= 0) onVotedChosen(voters)
        else {
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.voted_edit))
                .setMessage(getString(R.string.voted_edit_confirmation, voters + counted, voters))
                .setPositiveButton(getString(R.string.yes)) { _, _ -> onVotedChosen(voters, counted) }
                .setNegativeButton(getString(R.string.no)) { _, _ -> onVotedChosen(voters) }
                .show()
        }
    }

    private fun onVotedChosen(voters: Int, counted: Int = 0) {
        viewModel.onVotedChanged(voters, counted)
        binding.apply {
            votedEdit.visibility = View.GONE
            votedIconEdit.visibility = View.VISIBLE
            votedNumber.visibility = View.VISIBLE
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
        var mainMenu = R.menu.main_menu
        if (viewModel.isElectionFinished()) mainMenu = R.menu.main_menu_finished
        inflater.inflate(mainMenu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.finish_election -> {
                AlertDialog.Builder(context)
                    .setTitle(getString(R.string.election_finish))
                    .setMessage(getString(R.string.election_finish_confirmation))
                    .setPositiveButton(getString(R.string.yes)) { _, _ -> onFinishYes() }
                    .setNegativeButton(getString(R.string.no), null)
                    .show()
            }
            R.id.finish_viewing -> {
                AlertDialog.Builder(context)
                    .setTitle(getString(R.string.viewing_finish))
                    .setMessage(getString(R.string.viewing_finish_confirmation))
                    .setPositiveButton(getString(R.string.yes)) { _, _ -> onFinishYes(true) }
                    .setNegativeButton(getString(R.string.no), null)
                    .show()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun onFinishYes(isAlreadyFinished: Boolean = false) {
        navigateToTitle()
        viewModel.finishElection(isAlreadyFinished)
        if (!isAlreadyFinished)
            Toast.makeText(context, getString(R.string.election_finished), Toast.LENGTH_LONG).show()
    }


    private fun recordTurnout() {
        var task = TurnoutTask.getInstance()
        task?.cancel()

        val currentDate = Calendar.getInstance()

        val day = currentDate.get(Calendar.DAY_OF_MONTH)
        val hour = currentDate.get(Calendar.HOUR_OF_DAY)
        if ((hour > 21 || hour < 7) && viewModel.trainingStatus.value == Training.NO) return
        viewModel.onTurnoutRecorded()

        val startDate = Calendar.getInstance()
        startDate[Calendar.DAY_OF_MONTH] = day
        startDate[Calendar.HOUR_OF_DAY] = hour + 1
        startDate[Calendar.MINUTE] = 0
        startDate[Calendar.SECOND] = 5

        task = TurnoutTask.createInstance(viewModel, this)
        Timer().schedule(task, startDate.time,
            TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS))
    }

    fun createNotification(action: Action, totalVoters: Int) {
        val notificationManager = getSystemService(
            requireContext(),
            NotificationManager::class.java
        ) as NotificationManager

        createChannel(getString(R.string.turnout_id),
            getString(R.string.turnout_info),
            requireActivity()
        )

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = action.actionDate


        var turnout = action.actionTotal / totalVoters.toDouble() * 100.0
        turnout = maxOf(0.0, turnout)

        notificationManager.sendNotification(
            String.format(getString(R.string.turnout_notification),
                calendar[Calendar.HOUR_OF_DAY], calendar[Calendar.MINUTE], turnout),
            requireContext()
        )
    }

    private fun showHints(trainingStatus: Training) {
        if (trainingStatus == Training.FIRST) {
            HighlightPro.with(this)
                .setHighlightParameter { getHint(guideView.root, R.id.polling_station_icon_edit) }
                .setHighlightParameter { getHint(guideView.root, R.id.total_voters_icon_edit) }
                .setHighlightParameter {
                    getHint(
                        guideView.root, R.id.voted_icon_edit,
                        horizontalConstraints = Constraints.EndToEndOfHighlight
                    )
                }
                .setHighlightParameter { getHint(guideView.root, R.id.counter_button) }
                .setHighlightParameter {
                    getHint(
                        guideView.root, R.id.remove_button,
                        horizontalConstraints = Constraints.EndToEndOfHighlight
                    )
                }
                .setHighlightParameter {
                    getHint(
                        guideView.root, R.id.last_actions,
                        verticalConstraints = Constraints.BottomToTopOfHighlight
                    )
                }
                .setHighlightParameter { getHint(guideView.root, R.id.turnout_text) }
                .setHighlightParameter { getHint(guideView.root, R.id.turnout_hours) }
                .setBackgroundColor(R.color.black)
                .setOnShowCallback {
                    viewModel.guideTextChanged(ID_START_MAIN + it)
                }
                .show()
        } else {
            HighlightPro.with(this)
                .setHighlightParameter { getHint(guideView.root, R.menu.main_menu) }
                .setBackgroundColor(R.color.black)
                .setOnShowCallback {
                    viewModel.guideTextChanged(ID_START_MAIN + GAP + it)
                }
                .show()
        }
    }

    private fun exitApp() {
        AlertDialog.Builder(context)
            .setTitle(getString(R.string.exit))
            .setMessage(getString(R.string.exit_confirmation))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> finishAffinity(requireActivity()) }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}