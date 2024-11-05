package com.example.sicunetservicetest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.sicunetservicetest.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var tag = "MainActivity"

    private var value = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set the content view to the root of the binding object
        setContentView(binding.root)
        binding.buttonStartBle.setOnClickListener {
            Log.d(tag, "onCreate: clicked ${++value}")
        }
        val serviceIntent = Intent(this, MyForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
        binding.timerService.text = "${MyForegroundService.tickValue}"
    }
}