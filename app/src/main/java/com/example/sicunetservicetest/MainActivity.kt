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
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import com.hwit.HwitManager
import com.hwit.HwitManager.HwitSetIOValue


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
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
//        }
//        val serviceIntent = Intent(this, MyForegroundService::class.java)
        binding.buttonStartBle.setOnClickListener {
            Log.d(tag, "onCreate: clicked ${++value}")
            HwitSetIOValue(5, 1)
            //ContextCompat.startForegroundService(this, serviceIntent)
        }
        binding.buttonStopService.setOnClickListener {
//            Log.d(tag, "onCreate: clicked ${++value}")
//            applicationContext.stopService(serviceIntent)
            HwitSetIOValue(5, 0)
            Log.d(tag, "onCreate: stopped ${++value}")

            //stopLockTask()
        }
//        binding.timerService.text = "${MyForegroundService.tickValue}"
//        requestPermission()
//
//        //before run lock screen mode write the following command on adb is mandatory
//        //adb shell dpm set-device-owner com.example.sicunetservicetest/.MyDeviceAdminReceiver
//
//        startLockTaskMode()

        //to remove ownership following ownership command is needed
//        adb shell dpm remove-active-admin com.example.sicunetservicetest/.MyDeviceAdminReceiver
//        adb shell pm clear com.example.sicunetservicetest
//        adb shell pm uninstall com.example.sicunetservicetest

        //pinScreen()
    }

    private fun startLockTaskMode() {
        try {
            if (isDeviceOwner()) {
                startLockTask() // Locks the screen with this activity
            } else {
                Log.d("MyMessage", "App is not the Device Owner. Lock Task mode unavailable.")
                // Provide fallback or notify the user
                //println("App is not the Device Owner. Lock Task mode unavailable.")
            }
        }
        catch (e: Exception){
            Log.d("MyMessage", "${e.message}: ")
        }

    }

    private fun isDeviceOwner(): Boolean {
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(this, MyDeviceAdminReceiver::class.java)
        return dpm.isDeviceOwnerApp(packageName) && dpm.isAdminActive(adminComponent)
    }

//    private fun pinScreen() {
//        val flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
//                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
//                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
//                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                View.SYSTEM_UI_FLAG_FULLSCREEN or
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//        window.decorView.systemUiVisibility = flags
//    }

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