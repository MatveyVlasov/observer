package ru.elections.observer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.elections.observer.database.ElectionDatabaseDao
import java.lang.IllegalArgumentException

class ElectionViewModelFactory(
    private val database: ElectionDatabaseDao) : ViewModelProvider.Factory {

    @Suppress("unchecked cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ElectionViewModel::class.java)) {
            return ElectionViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}