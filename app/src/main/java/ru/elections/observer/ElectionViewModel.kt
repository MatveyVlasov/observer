package ru.elections.observer

import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.elections.observer.database.ACTIONS
import ru.elections.observer.database.Action
import ru.elections.observer.database.Election
import ru.elections.observer.database.ElectionDatabaseDao
import ru.elections.observer.main.MainFragment
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

    private var _showFinishElectionSnackbar = MutableLiveData<Boolean>()
    val showFinishElectionSnackbar: LiveData<Boolean>
        get() = _showFinishElectionSnackbar

    val actions = database.getAllActions()
    val timeActions = database.getTimeActions()


    init {
        Log.i("ElectionViewModel", "Init")
        initializeCurrentElection()
        _navigateToMainFragment.value = false
        _showFinishElectionSnackbar.value = false
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

    fun onNewElectionButton() {
        viewModelScope.launch {
            database.insert(Election())
            _currentElection.value = database.getCurrent()
            _navigateToMainFragment.value = true
        }
    }

    fun onCount() {
        viewModelScope.launch {
            _currentElection.value = currentElection.value?.also {
                ++it.counter
                database.update(it)
                database.insert(Action(electionId = it.electionId, actionType = ACTIONS.COUNT,
                    actionDate = System.currentTimeMillis(), actionTotal = it.counter))
            }
        }
    }

    fun onRemove() {
        viewModelScope.launch {
            _currentElection.value = currentElection.value?.also {
                if (it.counter <= 0) {
                    _showFinishElectionSnackbar.value = true
                    return@launch
                }
                --it.counter
                database.update(it)
                database.insert(Action(electionId = it.electionId, actionType = ACTIONS.REMOVE,
                    actionDate = System.currentTimeMillis(), actionTotal = it.counter))
            }
        }
    }

    fun onTurnoutRecorded(fragment: MainFragment, fromTimer: Boolean = true) {
        if (_currentElection.value?.counter == 0) return
        viewModelScope.launch {
            val lastRecord = database.getLastTimeAction()
            val timeDifference = System.currentTimeMillis() - (lastRecord?.actionDate ?: 0)
            val interval = TimeUnit.MILLISECONDS.convert(1, TimeUnit.HOURS)
            if (timeDifference + 1000 < interval) {
                return@launch
            }
            _currentElection.value = currentElection.value?.also {
                var time = System.currentTimeMillis() - (timeDifference % interval)
                if (timeDifference == System.currentTimeMillis()) time = timeDifference
                val action = Action(
                    electionId = it.electionId, actionType = ACTIONS.TIME,
                    actionDate = time,
                    actionTotal = it.counter
                )
                database.insert(action)
                if (fromTimer) {
                    fragment.createNotification(action, currentElection.value!!.totalVoters)
                }
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


    fun doneNavigating() {
        _navigateToMainFragment.value = false
    }

    fun doneShowingSnackbar() {
        _showFinishElectionSnackbar.value = false
    }

    fun finishElection() {
        viewModelScope.launch {
            _currentElection.value = currentElection.value?.also {
                it.isFinished = true
                database.update(it)
            }
            _currentElection.value = null
        }
    }

}
