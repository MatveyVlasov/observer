package ru.elections.observer.main

import android.content.Context
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.runBlocking
import ru.elections.observer.ElectionViewModel
import ru.elections.observer.database.ACTIONS
import ru.elections.observer.database.Action
import ru.elections.observer.database.ElectionDatabase
import ru.elections.observer.database.ElectionDatabaseDao
import java.util.*

//class TurnoutTask(val viewModel: ElectionViewModel) : TimerTask() {
//    override fun run() {
//        runBlocking {
//            val database = viewModel.database
//            database.getCurrent()?.let {
//                database.insert(Action(electionId = it.electionId, actionType = ACTIONS.TIME,
//                    actionDate = System.currentTimeMillis(), actionTotal = it.counter))
//
//            }
//        }
//    }
//
//    companion object {
//        private var INSTANCE: TurnoutTask? = null
//
//        fun getInstance(viewModel: ElectionViewModel): TurnoutTask {
//            synchronized(this) {
//                var instance = INSTANCE
//                if (instance == null) {
//                    instance = TurnoutTask(viewModel)
//                    INSTANCE = instance
//                }
//                return instance
//            }
//        }
//    }
//}

class TurnoutTask(private val viewModel: ElectionViewModel) : TimerTask() {
    override fun run() {
        viewModel.onTurnoutRecorded()
    }

    companion object {
        private var isInitialized = false

        fun getInstance(viewModel: ElectionViewModel): TurnoutTask? {
            if (isInitialized) return null
            isInitialized = true
            return TurnoutTask(viewModel)
        }
    }
}
