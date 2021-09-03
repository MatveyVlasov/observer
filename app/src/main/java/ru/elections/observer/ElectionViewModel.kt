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
        initializecurrentElection()
        _navigateToMainFragment.value = false
    }


    private fun initializecurrentElection() {
        viewModelScope.launch {
            _currentElection.value = database.getLast()
            size.value = database.getSize()
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
        viewModelScope.launch {
            val job: Job = viewModelScope.launch {
                database.insert(Election())
                initializecurrentElection()
            }
            job.join()
            _navigateToMainFragment.value = true
        }
    }

    fun doneNavigating() {
        _navigateToMainFragment.value = false
    }

    fun finishElection() {
        viewModelScope.launch {
            database.clear()
        }
    }

}