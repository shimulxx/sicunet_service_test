package com.example.sicunetservicetest

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.sicunetservicetest.databinding.ActivityMainBinding
import com.example.sicunetservicetest.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNotificationBinding

    private lateinit var player: MediaPlayer

    val tag = "notification activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(tag, "onCreate: called")
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        // Set the content view to the root of the binding object
        setContentView(binding.root)
        player = MediaPlayer.create(
            this,
            Settings.System.DEFAULT_RINGTONE_URI
        )
        player.setVolume(0.1f, 0.1f)
        player.start()
    }

    override fun onDestroy() {
        player.stop()
        super.onDestroy()
    }
}