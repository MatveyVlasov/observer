package ru.elections.observer.main

import ru.elections.observer.ElectionViewModel
import java.util.*

class TurnoutTask(private val viewModel: ElectionViewModel) : TimerTask() {
    override fun run() {
        viewModel.onTurnoutRecorded()
    }

    override fun cancel(): Boolean {
        INSTANCE = null
        return super.cancel()
    }

    companion object {
        private var INSTANCE: TurnoutTask? = null

        fun getInstance(): TurnoutTask? {
            return INSTANCE
        }

        fun createInstance(viewModel: ElectionViewModel): TurnoutTask {
            INSTANCE = TurnoutTask(viewModel)
            return INSTANCE!!
        }
    }
}
