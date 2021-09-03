package ru.elections.observer.database

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
    suspend fun clear()

    @Query("SELECT * FROM election_info ORDER BY electionId DESC LIMIT 1")
    suspend fun getLast(): Election?

    @Query("SELECT COUNT(*) FROM election_info")
    suspend fun getSize(): Int
}