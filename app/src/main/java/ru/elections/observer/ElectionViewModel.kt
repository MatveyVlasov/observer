package ru.elections.observer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ElectionViewModel : ViewModel() {
    private val _pollingStation = MutableLiveData<Int>()
    val pollingStation: LiveData<Int>
        get() = _pollingStation

    init {
        _pollingStation.value = -1
    }
}