package com.example.sicunetservicetest
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.audiofx.AcousticEchoCanceler
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sicunetservicetest.databinding.ActivityMainBinding
import com.hwit.HwitManager.HwitSetIOValue


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var TAG = "AppActivity"

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
        Log.d(TAG, "onCreate: called")
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        // Set the content view to the root of the binding object
        setContentView(binding.root)
//        val currentValue = HwitGetIOValue(5)
//        Log.d(tag, "onCreate: current value: $currentValue")
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
            val serviceIntent = Intent(this, MyForegroundService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)

//            HwitSetWifiStaticIpConnect(
//                this,
//                "Sicunet 5G",
//                "sicunet2025",
//                2,
//                "192.168.1.30",
//                "192.168.1.1",
//                "255.255.255.0",
//                "8.8.8.8",
//                "8.8.4.4"
//            )
        }
        //binding.timerService.text = "${MyForegroundService.tickValue}"
        binding.buttonStopService.setOnClickListener {
//            Log.d(tag, "onCreate: clicked ${++value}")
//            applicationContext.stopService(serviceIntent)
            //val result = HwitGetCpuTemp()
            //HwitSetIOValue(5, 0)
//            val result = HwitGetBoardEthIp(this)
            //Log.d(tag, "onCreate: stopped ${result}")

            //stopLockTask()

//            HwitSetWifiDhcpIpConnect(
//                this,
//                "Sicunet 5G",
//                "sicunet2025",
//                2,
//            )
            val serviceIntent = Intent(this, MyForegroundService::class.java)
            stopService(serviceIntent)
            //finish()
        }
        binding.timerService.text = "${MyForegroundService.tickValue}"
        //binding.timerService.text = Build.MODEL
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
        requestPermission3()
        //turnScreenOnAndKeyguardOff()
    }


    private fun requestPermission3(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            );
        }
    }

    private fun turnScreenOnAndKeyguardOff() {
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            (getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager).also {
                it.requestDismissKeyguard(this, null)
            }
        }

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
    }

    override fun onPause() {
        Log.d(TAG, "onPause: called")
        super.onPause()
    }

    override fun onResume() {
        Log.d(TAG, "onResume: called")
        super.onResume()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy: called")
        super.onDestroy()
    }

    override fun onStop() {
        Log.d(TAG, "onStop: called")
        super.onStop()
    }
    
    private fun work(){
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        if (AcousticEchoCanceler.isAvailable()) {
            val id = audioManager.generateAudioSessionId()
            val aec = AcousticEchoCanceler.create(id)
            aec?.setEnabled(true)
        }
    }

    fun disableEchoCanceler(audioSessionId: Int) {
        if (AcousticEchoCanceler.isAvailable()) {
            val echoCanceler = AcousticEchoCanceler.create(audioSessionId)
            echoCanceler.setEnabled(false)
            echoCanceler.release()
        }
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