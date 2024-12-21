package com.example.sicunetservicetest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sicunetservicetest.databinding.ActivityMainBinding
import com.hwit.HwitManager.HwitGetBoardEthIp
import com.hwit.HwitManager
import com.hwit.HwitManager.HwitGetCpuTemp
import com.hwit.HwitManager.HwitSetIOValue
import com.hwit.HwitManager.HwitGetIOValue
import com.hwit.HwitManager.HwitRebootSystem
import com.hwit.HwitManager.getAvailableCpuFreq


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var tag = "MainActivity"

    private var value = 0

    val PERMISSION_REQUEST_CODE: Int = 1


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with accessing phone state
            } else {
                // Permission denied, inform the user
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set the content view to the root of the binding object
        setContentView(binding.root)
        val currentValue = HwitGetIOValue(5)
        Log.d(tag, "onCreate: current value: $currentValue")
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
//        }
//        val serviceIntent = Intent(this, MyForegroundService::class.java)
        binding.buttonStartBle.setOnClickListener {
//            Log.d(tag, "onCreate: clicked ${++value}")
//            val powerManager = getSystemService(POWER_SERVICE) as PowerManager?
//            powerManager?.reboot(null)
            //HwitSetIOValue(5, 1)
            //HwitRebootSystem(this)
            //ContextCompat.startForegroundService(this, serviceIntent)
        }
        binding.buttonStopService.setOnClickListener {
//            Log.d(tag, "onCreate: clicked ${++value}")
//            applicationContext.stopService(serviceIntent)
            val result = HwitGetCpuTemp()
            //HwitSetIOValue(5, 0)
//            val result = HwitGetBoardEthIp(this)
            Log.d(tag, "onCreate: stopped ${result}")

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

        //requestPermission2()
    }

    private fun requestPermission2(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.REBOOT)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.REBOOT),
                PERMISSION_REQUEST_CODE
            );
        }
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