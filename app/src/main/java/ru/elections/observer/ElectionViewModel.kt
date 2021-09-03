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

    private var _lastElection = MutableLiveData<Election?>()
    val lastElection: LiveData<Election?>
        get() = _lastElection

    private var _navigateToMainFragment = MutableLiveData<Boolean>()
    val navigateToMainFragment: LiveData<Boolean>
        get() = _navigateToMainFragment

   var size = MutableLiveData<Int>()

    init {
        //viewModelScope.launch { database.clear() }
        Log.i("Current init", "It's INIT block")
        initializeLastElection()
        _navigateToMainFragment.value = false
    }


    private fun initializeLastElection() {
        Log.i("Current init", "Heq")
        viewModelScope.launch {
            _lastElection.value = database.getLast()
            size.value = database.getSize()
            Log.i("Current init", size.value.toString())
        }
    }

    fun onPollingStationChanged(station: Int) {
        viewModelScope.launch {
            _lastElection.value = _lastElection.value?.also {
                it.pollingStation = station
                database.update(it)
            }
        }
    }

    fun onNewElectionButton() {
        viewModelScope.launch {
            val job: Job = viewModelScope.launch {
                database.insert(Election())
                initializeLastElection()
            }
            job.join()
            _navigateToMainFragment.value = true
        }
    }

    fun doneNavigating() {
        _navigateToMainFragment.value = false
    }

}