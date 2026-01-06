package `in`.visheshraghuvanshi.clock.features.alarm.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val time: String,
    val label: String,
    val tag: String = "",
    val colorArgb: Int,
    val isActive: Boolean = true,
    val isSnoozeEnabled: Boolean = true,
    val isVibrationEnabled: Boolean = true
)