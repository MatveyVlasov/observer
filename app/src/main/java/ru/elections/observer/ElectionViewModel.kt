package ru.elections.observer

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

   var size = MutableLiveData<Int>()

    init {
        //viewModelScope.launch { database.clear() }
        Log.i("ElectionViewModel", "Init")
        initializeCurrentElection()
        _navigateToMainFragment.value = false
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

    fun doneNavigating() {
        _navigateToMainFragment.value = false
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