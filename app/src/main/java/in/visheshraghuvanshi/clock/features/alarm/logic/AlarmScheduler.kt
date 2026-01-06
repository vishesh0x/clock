package `in`.visheshraghuvanshi.clock.features.alarm.logic

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import `in`.visheshraghuvanshi.clock.features.alarm.data.AlarmEntity
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    fun schedule(alarm: AlarmEntity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(context, "Please allow 'Alarms & Reminders' permission", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                return
            }
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_LABEL", alarm.label)
            putExtra("ALARM_VIBRATE", alarm.isVibrationEnabled)
            putExtra("ALARM_SNOOZE", alarm.isSnoozeEnabled)
        }

        val parts = alarm.time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val now = LocalDateTime.now()
        var alarmTime = now.with(LocalTime.of(hour, minute))
        if (alarmTime.isBefore(now)) {
            alarmTime = alarmTime.plusDays(1)
        }

        val triggerTime = alarmTime.atZone(ZoneId.systemDefault()).toEpochSecond() * 1000

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTime, pendingIntent)

        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            Log.d("AlarmScheduler", "Alarm set for: $alarmTime")
        } catch (e: SecurityException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to set alarm: Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    fun cancel(alarm: AlarmEntity) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}