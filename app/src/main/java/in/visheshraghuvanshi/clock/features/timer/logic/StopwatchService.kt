package `in`.visheshraghuvanshi.clock.features.timer.logic

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
import androidx.core.app.NotificationCompat
import `in`.visheshraghuvanshi.clock.MainActivity
import `in`.visheshraghuvanshi.clock.R
import kotlinx.coroutines.*
import java.util.Locale

class StopwatchService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var job: Job? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val startTime = intent.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
                val accumulated = intent.getLongExtra(EXTRA_ACCUMULATED, 0L)
                startForegroundLoop(startTime, accumulated)
            }
            ACTION_PAUSE -> {
                val elapsed = intent.getLongExtra(EXTRA_ACCUMULATED, 0L)
                showPausedNotification(elapsed)
                job?.cancel()
            }
            ACTION_STOP -> stopService()
        }
        return START_STICKY
    }

    private fun startForegroundLoop(startTime: Long, accumulated: Long) {
        createChannel()
        startForegroundCompat(NOTIF_ID, buildNotification("Stopwatch Running"))

        job?.cancel()
        job = scope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val elapsed = (now - startTime) + accumulated
                updateNotification(formatStopwatch(elapsed))
                delay(1000)
            }
        }
    }

    private fun showPausedNotification(elapsed: Long) {
        createChannel()
        val notif = buildNotification("Paused: ${formatStopwatch(elapsed)}")
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, notif)
    }

    private fun stopService() {
        job?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun updateNotification(text: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, buildNotification(text))
    }

    private fun startForegroundCompat(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(id, notification)
        }
    }

    private fun buildNotification(text: String): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = Intent(this, StopwatchService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 1, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Stopwatch")
            .setContentText(text)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentIntent(contentPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(R.drawable.ic_launcher_foreground, "Stop", stopPendingIntent)
            .build()
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Stopwatch",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            setSound(null, null)
        }
        manager.createNotificationChannel(channel)
    }

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun formatStopwatch(millis: Long): String {
        val minutes = (millis / 1000) / 60
        val seconds = (millis / 1000) % 60
        return String.format(Locale.US, "%02d:%02d", minutes, seconds)
    }

    companion object {
        const val CHANNEL_ID = "stopwatch_channel_v2"
        const val NOTIF_ID = 101
        const val ACTION_START = "ACTION_START"
        const val ACTION_PAUSE = "ACTION_PAUSE"
        const val ACTION_STOP = "ACTION_STOP"
        const val EXTRA_START_TIME = "EXTRA_START_TIME"
        const val EXTRA_ACCUMULATED = "EXTRA_ACCUMULATED"
    }
}