package ru.elections.observer

import android.util.Log
import androidx.lifecycle.*
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.elections.observer.database.ACTIONS
import ru.elections.observer.database.Action
import ru.elections.observer.database.Election
import ru.elections.observer.database.ElectionDatabaseDao
import ru.elections.observer.main.MainFragment
import ru.elections.observer.past.PastFragment
import ru.elections.observer.past.PastFragmentDirections
import java.util.*
import java.util.concurrent.TimeUnit

class ElectionViewModel(
    val database: ElectionDatabaseDao) : ViewModel() {

    private var _currentElection = MutableLiveData<Election?>()
    val currentElection: LiveData<Election?>
        get() = _currentElection

    var isElectionInitialized = false

    private var _navigateToMainFragment = MutableLiveData<Boolean>()
    val navigateToMainFragment: LiveData<Boolean>
        get() = _navigateToMainFragment

    private var _navigateToPastFragment = MutableLiveData<Boolean>()
    val navigateToPastFragment: LiveData<Boolean>
        get() = _navigateToPastFragment

    private var _showSnackbar = MutableLiveData<Boolean>()
    val showSnackbar: LiveData<Boolean>
        get() = _showSnackbar

    var snackbarResources = 0

    var elections = database.getAllElections()
    val actions = database.getAllActions()
    val timeActions = database.getTimeActions()


    init {
        initializeCurrentElection()
        _showSnackbar.value = false
        _navigateToMainFragment.value = false
        _navigateToPastFragment.value = false
    }


    private fun initializeCurrentElection() {
        viewModelScope.launch {
            _currentElection.value = database.getCurrent()
            isElectionInitialized = true
            _currentElection.value = _currentElection.value // to call observer
        }
    }

    fun onPollingStationChanged(station: Int) {
        viewModelScope.launch {
            _currentElection.value = _currentElection.value?.also {
                it.pollingStation = station
                database.update(it)
            }
        }
    }

    fun onTotalVotersChanged(voters: Int) {
        viewModelScope.launch {
            _currentElection.value = _currentElection.value?.also {
                it.totalVoters = voters
                database.update(it)
            }
        }
    }

    fun onVotedChanged(voters: Int, counted: Int) {
        viewModelScope.launch {
            _currentElection.value = _currentElection.value?.also {
                // to set initial time action
                if (it.counter == 0 && database.getLastTimeAction() == null) {
                    onTurnoutRecorded(firstRecord = true)
                    delay(500)
                }
                it.voted = voters
                it.counter = voters + counted
                database.update(it)
                database.insert(Action(electionId = it.electionId,
                    actionType = if (counted > 0) ACTIONS.ADD else ACTIONS.SET,
                    actionDate = System.currentTimeMillis(), actionTotal = it.counter))
            }
        }
    }

    fun onOfficialChanged(actionId: Long, officialTotal: Int) {
        viewModelScope.launch {
            _currentElection.value = currentElection.value?.also {
                val action = database.getAction(actionId)
                action?.let {
                    it.officialTotal = officialTotal
                    database.update(it)
                }
            }
        }
    }

    fun onCurrentElectionChanged(election: Election, fragment: PastFragment) {
        viewModelScope.launch {
            election.isCurrent = true
            database.update(election)
            _currentElection.value = election
            isElectionInitialized = true

            fragment.findNavController()
                .navigate(PastFragmentDirections.actionPastFragmentToMainFragment())
        }
    }

    fun onElectionDeleted(election: Election) {
        viewModelScope.launch {
            database.delete(election)
        }
    }

    fun onNewElectionButton() {
        viewModelScope.launch {
            database.insert(Election())
            _currentElection.value = database.getCurrent()
            _navigateToMainFragment.value = true
        }
    }

    fun onPastElectionsButton() {
        _navigateToPastFragment.value = true
    }

    fun onCount() {
        if (isElectionFinished(true)) return
        viewModelScope.launch {
            _currentElection.value = currentElection.value?.also {
                // to set initial time action
                if (it.counter == 0 && database.getLastTimeAction() == null) {
                    onTurnoutRecorded(firstRecord = true)
                    delay(500)
                }
                ++it.counter
                database.update(it)
                database.insert(Action(electionId = it.electionId, actionType = ACTIONS.COUNT,
                    actionDate = System.currentTimeMillis(), actionTotal = it.counter))
            }
        }
    }

    fun onRemove() {
        if (isElectionFinished(true)) return
        viewModelScope.launch {
            _currentElection.value = currentElection.value?.also {
                if (it.counter <= 0) {
                    snackbarResources = R.string.negative_counter
                    _showSnackbar.value = true
                    return@launch
                }
                --it.counter
                database.update(it)
                database.insert(Action(electionId = it.electionId, actionType = ACTIONS.REMOVE,
                    actionDate = System.currentTimeMillis(), actionTotal = it.counter))
            }
        }
    }

    fun onTurnoutRecorded(fragment: MainFragment? = null, firstRecord: Boolean = false) {
        if (_currentElection.value == null) return
        if (isElectionFinished()) return
        if (_currentElection.value?.counter == 0 && !firstRecord) return
        viewModelScope.launch {
            val lastRecord = database.getLastTimeAction()
            val timeDifference = System.currentTimeMillis() - (lastRecord?.actionDate ?: 0)
            val interval = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
            if (timeDifference + 1000 < interval) {
                return@launch
            }

            _currentElection.value = currentElection.value?.also {
                val time = System.currentTimeMillis() - (timeDifference % interval)

                val action = Action(
                    electionId = it.electionId, actionType = ACTIONS.TIME,
                    actionDate = System.currentTimeMillis() - (timeDifference % interval),
                    actionTotal = it.counter
                )
                database.insert(action)

                if (firstRecord) {
                    it.dateStart = time
                    database.update(it)
                }

                fragment?.createNotification(action, currentElection.value!!.totalVoters)
            }
        }
    }

    fun getTurnout(): Double {
        var turnout = 0.0
        _currentElection.value?.let {
            turnout = it.counter / it.totalVoters.toDouble() * 100.0
        }
        turnout = maxOf(turnout, 0.0)
        return turnout
    }


    fun doneNavigatingToMain() {
        _navigateToMainFragment.value = false
    }

    fun doneNavigatingToPast() {
        _navigateToPastFragment.value = false
    }

    fun doneShowingSnackbar() {
        _showSnackbar.value = false
    }

    fun isElectionFinished(notification: Boolean = false): Boolean {
        if ((_currentElection.value?.dateEnd ?: 0L) == 0L) return false
        if (notification) {
            snackbarResources = R.string.election_already_finished
            _showSnackbar.value = true
        }
        return true
    }

    fun finishElection(isAlreadyFinished: Boolean = false) {
        viewModelScope.launch {
            _currentElection.value = currentElection.value?.also {
                it.isCurrent = false
                if (!isAlreadyFinished) it.dateEnd = System.currentTimeMillis()
                database.update(it)
            }
            _currentElection.value = null
        }
    }
}
