package `in`.visheshraghuvanshi.clock.features.alarm.logic

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent == null) return
        
        // Validate intent action and source
        if (intent.action != "android.intent.action.BOOT_COMPLETED" && 
            intent.action != "android.intent.action.ALARM_TRIGGERED") {
            return
        }
        
        val serviceIntent = Intent(context, AlarmService::class.java).apply {
            action = "START_ALARM"
            // Only pass safe, validated extras
            intent.getLongExtra("alarm_id", -1).takeIf { it != -1L }?.let {
                putExtra("alarm_id", it)
            }
        }
        context.startForegroundService(serviceIntent)
    }
}