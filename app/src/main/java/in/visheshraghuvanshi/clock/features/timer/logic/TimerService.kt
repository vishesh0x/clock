package `in`.visheshraghuvanshi.clock.features.timer.logic

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import `in`.visheshraghuvanshi.clock.MainActivity
import `in`.visheshraghuvanshi.clock.R
import `in`.visheshraghuvanshi.clock.features.alarm.ui.AlarmTriggerActivity
import kotlinx.coroutines.*
import java.util.Locale

class TimerService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Clock:TimerWakeLock")
        wakeLock?.acquire(20 * 60 * 1000L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val duration = intent.getLongExtra(EXTRA_DURATION, 0L)
                startForegroundTimer(duration)
            }
            ACTION_STOP -> stopTimer()
        }
        return START_NOT_STICKY
    }

    private fun startForegroundTimer(durationSec: Long) {
        val endTime = System.currentTimeMillis() + (durationSec * 1000)
        createNotificationChannels()

        startForegroundCompat(NOTIFICATION_ID, buildSilentNotification("Timer Started"))

        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                val remainingMillis = endTime - System.currentTimeMillis()

                if (remainingMillis <= 0) {
                    triggerAlarm()
                    break
                }

                updateNotification(formatTime(remainingMillis / 1000))
                delay(1000)
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    private fun triggerAlarm() {
        val fullScreenIntent = Intent(this, AlarmTriggerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_NO_USER_ACTION
            putExtra("EXTRA_IS_TIMER", true)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE_ALARM,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmNotification = NotificationCompat.Builder(this, CHANNEL_ID_ALARM)
            .setContentTitle("Timer Finished")
            .setContentText("00:00")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setAutoCancel(false)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Dismiss", stopPendingIntent())
            .build()

        startForegroundCompat(NOTIFICATION_ID, alarmNotification)

        scope.launch {
            delay(10 * 60 * 1000L)
            stopTimer()
        }
    }

    private fun startForegroundCompat(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(id, notification)
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (_: Exception) { }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildSilentNotification(text))
    }

    private fun stopPendingIntent(): PendingIntent {
        val stopIntent = Intent(this, TimerService::class.java).apply {
            action = ACTION_STOP
        }
        return PendingIntent.getService(
            this,
            REQUEST_CODE_STOP,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun buildSilentNotification(content: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_SILENT)
            .setContentTitle("Timer Running")
            .setContentText(content)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent())
            .build()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val silentChannel = NotificationChannel(
            CHANNEL_ID_SILENT,
            "Timer Countdown",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
        }
        manager.createNotificationChannel(silentChannel)

        val alarmChannel = NotificationChannel(
            CHANNEL_ID_ALARM,
            "Timer Alarm",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
        }
        manager.createNotificationChannel(alarmChannel)
    }

    private fun formatTime(seconds: Long): String {
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    override fun onDestroy() {
        scope.cancel()
        try {
            if (wakeLock?.isHeld == true) wakeLock?.release()
        } catch (_: Exception) { }
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID_SILENT = "timer_channel_silent_v2"
        const val CHANNEL_ID_ALARM = "timer_channel_alarm_v2"
        const val NOTIFICATION_ID = 42
        const val REQUEST_CODE_ALARM = 100
        const val REQUEST_CODE_STOP = 101

        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_DURATION = "EXTRA_DURATION"
    }
}