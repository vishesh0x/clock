package `in`.visheshraghuvanshi.clock.features.alarm.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = "START_ALARM"
            putExtras(intent?.extras ?: return)
        }
        context.startForegroundService(serviceIntent)
    }
}