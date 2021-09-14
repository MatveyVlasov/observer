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
import ru.elections.observer.utils.*
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

    private var _trainingStatus = MutableLiveData<Training>()
    val trainingStatus: LiveData<Training>
        get() = _trainingStatus

    private var _guideText = MutableLiveData<String>()
    val guideText: LiveData<String>
        get() = _guideText

    var snackbarResources = 0

    var elections = database.getAllElections()
    val actions = database.getAllActions()
    val timeActions = database.getTimeActions()


    init {
        initializeCurrentElection()
        _showSnackbar.value = false
        _navigateToMainFragment.value = false
        _navigateToPastFragment.value = false
        _trainingStatus.value = Training.NO
        _guideText.value = ""
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

    fun onTrainingButton() {
        _trainingStatus.value = Training.FIRST
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

    fun guideTextChanged(step: Int) {
        val str = when (step) {
            ID_START_TITLE -> "Нажмите сюда, чтобы начать выборы"
            ID_START_TITLE + GAP -> "Нажмите сюда, чтобы посмотреть информацию о прошедших выборах"
            ID_START_MAIN -> {"""
                Здесь указывается номер УИК
                Его можно изменить, нажав на иконку справа
                После ввода номера нужно нажать Done на клавиатуре
                Пусть наш УИК имеет номер 1020
            """.trimIndent()
            }
            ID_START_MAIN + 1 -> {
                onPollingStationChanged(1020)
                "Пусть количество избирателей, прикреплённых к УИК, равно 1984"
            }
            ID_START_MAIN + 2 -> {
                onTotalVotersChanged(1984)
                """
                    Этот пункт пригодится, если вы начали наблюдение не с начала выборов
                    Если вы сразу забыли вписать нужное число, это можно сделать позже
                    Допустим, до начала нашего наблюдения проголосовало 212 избирателей
            """.trimIndent()
            }
            ID_START_MAIN + 3 -> {
                onVotedChanged(212, 0)
                "Это - самая главная кнопка. Нажмите, чтобы посчитать избирателя"
            }
            ID_START_MAIN + 4 -> {
                onCount()
                """
                    Эта кнопка уменьшает число проголосовавших на единицу
                    Она используется в случае ошибочного подсчёта избирателя
                """.trimIndent()
            }
            ID_START_MAIN + 5 -> "Здесь записаны все последние действия"
            ID_START_MAIN + 6 -> "Здесь показана явка на данный момент"
            ID_START_MAIN + 7 -> "Нажмите на иконку, чтобы узнать явку к каждому часу"
            ID_START_MAIN + GAP -> "С помощью меню справа завершите выборы"
            ID_START_TURNOUT -> "В таблице находится информация о явке к каждому часу с момента начала выборов"
            ID_START_TURNOUT + 1 -> "Кливнув на иконку, можно указать явку по данным комиссии"
            ID_START_TURNOUT + 2 -> {
                _trainingStatus.value = Training.SECOND
                "Чтобы вернуться на главный экран выборов, нажмите на стрелочку слева"
            }
            ID_START_PAST -> "Здесь указаны даты начала и окончания выборов"
            ID_START_PAST + 1 -> {
                """
                    Для просмотра подробной информации о выборах, нажмите на данную иконку
                    Вы можете посмотреть всю информацию, которая была доступна до завершения выборов
                    Однако вы не можете ничего редактировать, кроме явки по данным комиссии
                """.trimIndent()
            }
            ID_START_PAST + 2 -> {
                _trainingStatus.value = Training.NO
                """
                    Данная иконка предназначена для удаления записи о выборах
                    Удаляемся и мы, закончив обучение
                    Удачного наблюдения!
                """.trimIndent()
            }
            else -> "Ошибка"
        }
        _guideText.value = str
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
