package `in`.visheshraghuvanshi.clock.core.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import `in`.visheshraghuvanshi.clock.features.alarm.data.AlarmDao
import `in`.visheshraghuvanshi.clock.features.alarm.data.AlarmEntity

@Database(entities = [AlarmEntity::class], version = 2, exportSchema = false)
abstract class ClockDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: ClockDatabase? = null

        fun getDatabase(context: Context): ClockDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ClockDatabase::class.java,
                    "clock_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}