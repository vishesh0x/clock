package `in`.visheshraghuvanshi.clock.features.alarm.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = "START_ALARM"
            putExtra("ALARM_ID", intent.getIntExtra("ALARM_ID", 0))
            putExtra("ALARM_LABEL", intent.getStringExtra("ALARM_LABEL") ?: "Alarm")
            putExtra("ALARM_VIBRATE", intent.getBooleanExtra("ALARM_VIBRATE", true))
            putExtra("ALARM_SNOOZE", intent.getBooleanExtra("ALARM_SNOOZE", true))
        }
        context.startForegroundService(serviceIntent)
    }
}