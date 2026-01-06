package `in`.visheshraghuvanshi.clock.features.alarm.logic

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import `in`.visheshraghuvanshi.clock.R
import `in`.visheshraghuvanshi.clock.features.alarm.ui.AlarmTriggerActivity

class AlarmService : Service() {

    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val label = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val alarmId = intent?.getIntExtra("ALARM_ID", 0) ?: 0
        val shouldVibrate = intent?.getBooleanExtra("ALARM_VIBRATE", true) ?: true
        val canSnooze = intent?.getBooleanExtra("ALARM_SNOOZE", true) ?: true

        when (action) {
            "STOP_ALARM" -> {
                stopSelf()
                return START_NOT_STICKY
            }
            "SNOOZE_ALARM" -> {
                snoozeAlarm(alarmId, label)
                stopSelf()
                return START_NOT_STICKY
            }
        }

        try {
            val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ringtone = RingtoneManager.getRingtone(applicationContext, alarmUri)
            ringtone?.play()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (shouldVibrate) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibrator = vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(longArrayOf(0, 1000, 1000), 0)
                vibrator?.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 1000, 1000), 0)
            }
        }

        startForeground(1, createNotification(label, canSnooze))

        return START_STICKY
    }

    private fun snoozeAlarm(id: Int, label: String) {
        val alarmManager = getSystemService(AlarmManager::class.java)
        val intent = Intent(this, AlarmReceiver::class.java).apply {
            putExtra("ALARM_ID", id)
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_VIBRATE", true)
            putExtra("ALARM_SNOOZE", true)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val triggerTime = System.currentTimeMillis() + 10 * 60 * 1000

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }

    private fun createNotification(label: String, canSnooze: Boolean): Notification {
        val channelId = "ALARM_CHANNEL"
        val manager = getSystemService(NotificationManager::class.java)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Alarms", NotificationManager.IMPORTANCE_HIGH)
            channel.setSound(null, null)
            channel.enableVibration(false)
            manager.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, AlarmService::class.java).apply { action = "STOP_ALARM" }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val snoozeIntent = Intent(this, AlarmService::class.java).apply { action = "SNOOZE_ALARM" }
        val snoozePendingIntent = PendingIntent.getService(this, 1, snoozeIntent, PendingIntent.FLAG_IMMUTABLE)

        val fullScreenIntent = Intent(this, AlarmTriggerActivity::class.java).apply {
            putExtra("ALARM_LABEL", label)
            putExtra("ALARM_SNOOZE", canSnooze)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this, 0, fullScreenIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Alarm")
            .setContentText(label)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", stopPendingIntent)

        if (canSnooze) {
            builder.addAction(R.drawable.ic_launcher_foreground, "Snooze", snoozePendingIntent)
        }

        return builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        ringtone?.stop()
        vibrator?.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}