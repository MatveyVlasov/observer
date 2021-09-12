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

    @ColumnInfo(name = "voted")
    var voted: Int = 0,

    @ColumnInfo(name = "counter")
    var counter: Int = 0,

    @ColumnInfo(name = "is_current")
    var isCurrent: Boolean = true,

    @ColumnInfo(name = "date_start")
    var dateStart: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "date_end")
    var dateEnd: Long = 0
)