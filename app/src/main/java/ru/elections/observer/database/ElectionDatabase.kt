package ru.elections.observer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Election::class], version = 3, exportSchema = false)
abstract class ElectionDatabase : RoomDatabase() {
    abstract val electionDatabaseDao: ElectionDatabaseDao

    companion object {
        @Volatile
        private var INSTANCE: ElectionDatabase? = null

        fun getInstance(context: Context): ElectionDatabase {
            synchronized(this) {
                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(context.applicationContext,
                        ElectionDatabase::class.java, "election_info")
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}