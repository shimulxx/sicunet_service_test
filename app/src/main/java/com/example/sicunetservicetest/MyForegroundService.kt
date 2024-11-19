package com.example.sicunetservicetest

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat


class MyForegroundService : Service() {
    private val tag = "MyForegroundService"

    companion object {
        private const val CHANNEL_ID = "ForegroundServiceChannel"
        var tickValue = 0
    }

    private val timer = object : CountDownTimer(3000000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            Log.d(tag, "onTick: ${++tickValue}")
        }

        override fun onFinish() {
            Log.d(tag, "onFinish: called")
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        timer.start()
    }

    override fun onDestroy() {
        timer.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(
                NotificationManager::class.java
            )
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("My Service")
            .setContentText("Running in the background")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Makes the notification non-dismissible
            .build()

        startForeground(1, notification) // Start the service in the foreground with the notification
        return START_STICKY
    }
}