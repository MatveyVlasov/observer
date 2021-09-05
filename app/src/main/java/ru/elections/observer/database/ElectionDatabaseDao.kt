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

    @Query("SELECT * FROM election_info WHERE not is_finished ORDER BY electionId DESC LIMIT 1")
    suspend fun getCurrent(): Election?

    @Query("SELECT COUNT(*) FROM election_info")
    suspend fun getSize(): Int

    @Insert
    suspend fun insert(action: Action)

    @Update
    suspend fun update(action: Action)

    @Query("DELETE FROM actions")
    suspend fun clearActions()

    @Query("""SELECT * FROM actions WHERE actions.electionId =
         (SELECT electionId FROM election_info WHERE not is_finished ORDER BY electionId DESC LIMIT 1) 
          ORDER BY actionId DESC""")
    fun getAllActions(): LiveData<List<Action>>
}