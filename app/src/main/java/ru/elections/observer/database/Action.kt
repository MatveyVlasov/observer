package ru.elections.observer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ACTIONS {
    COUNT, REMOVE, SET, ADD
}


@Entity(tableName = "actions")
data class Action(
    @PrimaryKey(autoGenerate = true)
    var actionId: Long = 0L,

    @ColumnInfo(name = "electionId")
    var electionId: Long,

    @ColumnInfo(name = "action_type")
    var actionType: ACTIONS = ACTIONS.COUNT,

    @ColumnInfo(name = "action_date")
    var actionDate: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "action_total")
    var actionTotal: Int
)