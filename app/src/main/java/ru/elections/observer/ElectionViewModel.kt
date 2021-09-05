package ru.elections.observer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.elections.observer.database.ACTIONS
import ru.elections.observer.database.Action
import ru.elections.observer.database.Election
import ru.elections.observer.database.ElectionDatabaseDao

class ElectionViewModel(
    val database: ElectionDatabaseDao) : ViewModel() {

    private var _currentElection = MutableLiveData<Election?>()
    val currentElection: LiveData<Election?>
        get() = _currentElection

    private var _navigateToMainFragment = MutableLiveData<Boolean>()
    val navigateToMainFragment: LiveData<Boolean>
        get() = _navigateToMainFragment

    private var _showSnackbarEvent = MutableLiveData<Boolean>()
    val showSnackbarEvent: LiveData<Boolean>
        get() = _showSnackbarEvent

    val actions = database.getAllActions()
    // val actions = database.getAllActions()

    var size = MutableLiveData<Int>()

    init {
        Log.i("ElectionViewModel", "Init")
        initializeCurrentElection()
        _navigateToMainFragment.value = false
        _showSnackbarEvent.value = false
    }


    private fun initializeCurrentElection() {
        viewModelScope.launch {
            Log.i("ElectionViewModel", "Initializing...")
            _currentElection.value = database.getCurrent()
            size.value = database.getSize()
            Log.i("ElectionViewModel", _currentElection.value.toString())
            Log.i("ElectionViewModel", "Done!")
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

    fun onNewElectionButton() {
        Log.i("ElectionViewModel", "OnNewElectionButton")
        viewModelScope.launch {
            Log.i("ElectionViewModel", "Inserting...")
            database.insert(Election())
            _currentElection.value = database.getCurrent()
            Log.i("ElectionViewModel", _currentElection.value.toString())
            Log.i("ElectionViewModel", "Job done")
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
                    _showSnackbarEvent.value = true
                    return@launch
                }
                --it.counter
                database.update(it)
                database.insert(Action(electionId = it.electionId, actionType = ACTIONS.REMOVE,
                    actionDate = System.currentTimeMillis(), actionTotal = it.counter))
            }
        }
    }

    fun getTurnout(): String {
        var turnout = 0.0
        _currentElection.value?.let {
            turnout = it.counter / it.totalVoters.toDouble() * 100.0
        }
        turnout = maxOf(turnout, 0.0)
        return String.format("Явка: %2.2f %%", turnout)
    }

    fun doneNavigating() {
        _navigateToMainFragment.value = false
    }

    fun doneShowingSnackbar() {
        _showSnackbarEvent.value = false
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