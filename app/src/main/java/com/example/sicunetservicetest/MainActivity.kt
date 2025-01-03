package com.example.sicunetservicetest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sicunetservicetest.databinding.ActivityMainBinding
import android.Manifest
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var tag = "MainActivity"

    private var value = 0

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set the content view to the root of the binding object
        setContentView(binding.root)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
        }
        val serviceIntent = Intent(this, MyForegroundService::class.java)
        binding.buttonStartBle.setOnClickListener {
            Log.d(tag, "onCreate: clicked ${++value}")
            ContextCompat.startForegroundService(this, serviceIntent)
        }
        binding.buttonStopService.setOnClickListener {
            Log.d(tag, "onCreate: clicked ${++value}")
            applicationContext.stopService(serviceIntent)
        }
        binding.timerService.text = "${MyForegroundService.tickValue}"
        requestPermission()
    }

    private fun requestPermission() {
        val overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // Check the result of the permission request
            if (Settings.canDrawOverlays(this)) {
                //onOverlayPermissionGranted()
            } else {
                //onOverlayPermissionDenied()
            }
        }
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:" + this.packageName)
            )
            overlayPermissionLauncher.launch(intent)
            //startActivityForResult(intent, 232)
        } else {
            //Permission Granted-System will work
        }
    }
}