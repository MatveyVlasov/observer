package ru.elections.observer.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ElectionDatabaseDao {
    @Insert
    suspend fun insert(election: Election)

    @Update
    suspend fun update(election: Election)

    @Query("DELETE FROM election_info")
    suspend fun clearElection()

    @Query("SELECT * FROM election_info ORDER BY electionId DESC")
    fun getAllElections(): LiveData<List<Election>>

    @Query("SELECT * FROM election_info WHERE is_current ORDER BY electionId DESC LIMIT 1")
    suspend fun getCurrent(): Election?

    @Query("SELECT * FROM election_info WHERE electionId = :electionId")
    fun getElection(electionId: Long): Election?

    @Insert
    suspend fun insert(action: Action)

    @Update
    suspend fun update(action: Action)

    @Query("DELETE FROM actions")
    suspend fun clearActions()

    @Query("SELECT * FROM actions WHERE actionId = :actionId")
    suspend fun getAction(actionId: Long): Action?

    @Query("""SELECT * FROM actions WHERE actions.electionId =
         (SELECT electionId FROM election_info WHERE is_current ORDER BY electionId DESC LIMIT 1) 
          ORDER BY actionId DESC""")
    fun getAllActions(): LiveData<List<Action>>

    @Query("""SELECT * FROM actions WHERE action_type = :type AND actions.electionId =
         (SELECT electionId FROM election_info WHERE is_current ORDER BY electionId DESC LIMIT 1) 
          ORDER BY actionId DESC""")
    fun getTimeActions(type: ACTIONS = ACTIONS.TIME): LiveData<List<Action>>

    @Query("""SELECT * FROM actions WHERE action_type = :type AND actions.electionId =
         (SELECT electionId FROM election_info WHERE is_current ORDER BY electionId DESC LIMIT 1) 
          ORDER BY actionId DESC LIMIT 1""")
    suspend fun getLastTimeAction(type: ACTIONS = ACTIONS.TIME): Action?
}