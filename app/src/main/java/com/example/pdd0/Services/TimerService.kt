package com.example.pdd0.Services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.pdd0.R
import java.util.Timer
import java.util.TimerTask

class TimerService : Service() {

    private var timer: Timer? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, buildNotification()) // Показ уведомления о работе сервиса

        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {

            }
        }, 0, 1000) // Обновление таймера rаждую секунду

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun buildNotification(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            "timer_channel", "Timer Notifications", NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(notificationChannel)

        return NotificationCompat.Builder(this, "timer_channel")
            .setContentTitle("Timer Running")
            .setContentText("The exam timer is running in the background.")
            .setSmallIcon(R.drawable.ic_pr)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        timer?.cancel()
        super.onDestroy()
    }
}
