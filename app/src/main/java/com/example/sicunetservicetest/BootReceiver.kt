package com.example.sicunetservicetest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build


class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val serviceIntent = Intent(context, MyForegroundService::class.java)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // For Android 8.0 and above, use startForegroundService to start foreground services
                context.startForegroundService(serviceIntent)
            } else {
                context.startService(serviceIntent)
            }
        }
    }
}
