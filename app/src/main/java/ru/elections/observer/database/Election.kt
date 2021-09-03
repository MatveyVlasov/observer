package ru.elections.observer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "election_info")
data class Election(
    @PrimaryKey(autoGenerate = true)
    var electionId: Long = 0L,

    @ColumnInfo(name = "polling_station")
    var pollingStation: Int = -1,

    @ColumnInfo(name = "total_voters")
    var totalVoters: Int = -1,

    @ColumnInfo(name = "is_finished")
    var isFinished: Boolean = false
) {

}