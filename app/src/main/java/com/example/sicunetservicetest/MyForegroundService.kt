package com.example.sicunetservicetest

import android.Manifest
import android.R
import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.CountDownTimer
import android.os.IBinder
import android.os.SystemClock
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.PhoneAccountHandle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import kotlin.random.Random

class MyForegroundService : Service() {
    private val tag = "MyForegroundService"

    companion object {
        private const val CHANNEL_ID = "ForegroundServiceChannel"
        var tickValue = 0
    }

    private val timer = object : CountDownTimer(3000000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            Log.d(tag, "onTick: ${++tickValue}")
            if(tickValue > 0 && tickValue % 13 == 0){
                showFullScreenNotification()
            }
        }

        override fun onFinish() {
            Log.d(tag, "onFinish: called")
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(tag, "onCreate: called")
        timer.start()
    }

    override fun onDestroy() {
        timer.cancel()
        Log.d(tag, "onDestroy: called")
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
            .setSmallIcon(R.drawable.btn_plus)
            .setContentIntent(pendingIntent)
            .setOngoing(true) // Makes the notification non-dismissible
            .build()

        startForeground(1, notification) // Start the service in the foreground with the notification
        //test()
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.S)
    fun test(){
        val incomingCaller = Person.Builder()
            .setName("Jane Doe")
            .setImportant(true)
            .build()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)

        val notification = Notification.Builder(this, CHANNEL_ID)
            .setContentIntent(pendingIntent)
            .setSmallIcon(R.drawable.btn_plus)
            .setStyle(Notification.CallStyle.forOngoingCall(incomingCaller, pendingIntent))
            .addPerson(incomingCaller)
            .build()
        startForeground(1, notification)
    }

    fun showFullScreenNotification() {
        val channelId = "fullscreen_channel"
        val channelName = "Full Screen Notifications"

        // Create the notification channel (for Android O and higher)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for full-screen intents"
            }
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Intent for the activity to launch
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Incoming Call")
            .setContentText("Tap to answer")
            .setSmallIcon(R.drawable.ic_dialog_info)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(pendingIntent, true)
            .setAutoCancel(true)
            .build()

        // Show the notification
        // need to check permission
        with(NotificationManagerCompat.from(this)) {
            notify(Random.nextInt(), notification)
        }
    }
}