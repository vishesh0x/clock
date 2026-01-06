package `in`.visheshraghuvanshi.clock.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val label: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector
) {
    object Clock : Screen("clock", "Clock", Icons.Filled.Schedule, Icons.Outlined.Schedule)
    object Alarm : Screen("alarm", "Alarm", Icons.Filled.Alarm, Icons.Outlined.Alarm)
    object Timer : Screen("timer", "Timer", Icons.Filled.Timer, Icons.Outlined.Timer)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
    object EditAlarm : Screen("edit_alarm/{alarmId}", "Edit", Icons.Filled.Settings, Icons.Outlined.Settings) {
        fun createRoute(alarmId: Int) = "edit_alarm/$alarmId"
    }
}