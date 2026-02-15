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
import com.dk.uartnfc.DeviceManager.UartNfcDevice
import com.example.sicunetservicetest.databinding.ActivityMainBinding
import com.peripheral.library.PhController
import com.sdk.api.manager.ApiManager


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var tag = "MainActivity"

    private var value = 0

    val PERMISSION_REQUEST_CODE: Int = 1


    private var apiManager: ApiManager? = null

    private var uartNfcDevice: UartNfcDevice? = null

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

    private fun exec(cmd: String): String {
        return try {
            Runtime.getRuntime().exec(cmd).inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    private fun execRoot(cmd: String): String {
        return try {
            val p = Runtime.getRuntime().exec("su")
            p.outputStream.write("$cmd\nexit\n".toByteArray())
            p.outputStream.flush()
            p.inputStream.bufferedReader().readText()
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        apiManager = ApiManager.getInstance(this)

        // Set the content view to the root of the binding object
        setContentView(binding.root)
        Log.d(tag, "onCreate: current value:")
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
//        }
//        val serviceIntent = Intent(this, MyForegroundService::class.java)
        binding.buttonStartBle.setOnClickListener {
            //apiManager?.setBacklightBright(0)

            val result = execRoot("whoami")
            val result2 = execRoot("getprop ro.product.model")

            Log.d("SHELL RESULT", "onCreate: $result")
            Log.d("SHELL RESULT", "onCreate: $result2")

            //PhController.relay_Control_Open()
//            PhController.red_Led_Open()
//            PhController.whiteLight_Control_Open(this)

//            Log.d("SDK TESTING", "BT MAC: ${apiManager?.btMac}")
//            Log.d("SDK TESTING", "WIFI MAC: ${apiManager?.wifiMac}")
//            Log.d("SDK TESTING", "LAN MAC: ${apiManager?.ethMac}")

//            Log.d("TEST WORK Door Sensor", "onCreate: ${apiManager?.dsStatus}") //Door Sensor
//////
//////            Log.d("TEST WORK DO", "onCreate: ${apiManager?.doorbell}")
////
//            Log.d("TEST WORK Door Open", "onCreate: ${apiManager?.openDoorStatus}") //Open Button
////
//            Log.d("TEST WORK BELL", "onCreate: ${apiManager?.doorbell}") //Door Bell

//            Log.d(tag, "onCreate: clicked ${++value}")
//            val powerManager = getSystemService(POWER_SERVICE) as PowerManager?
//            powerManager?.reboot(null)
            //HwitSetIOValue(5, 1)
            //HwitRebootSystem(this)
            //ContextCompat.startForegroundService(this, serviceIntent)

//            HwitSetWifiStaticIpConnect(
//                this,
//                "Sicunet 5G",
//                "sicunet2025",
//                2,
//                "192.168.1.200",
//                "192.168.1.1",
//                "255.255.255.0",
//                "8.8.8.8",
//                "8.8.4.4"
//            )

//            HwitSetLocalIP(
//                this,
//                "192.168.1.50",
//                "eth0",
//                0,
//            )
        }
        binding.buttonStopService.setOnClickListener {

//            PhController.relay_Control_Close()
//            PhController.green_Led_Open()
//            PhController.whiteLight_Control_Close(this)



//            apiManager?.setBacklightBright(1)
//            apiManager?.screenOffTimeout = 1
            //apiManager?.setWifiEnabled(false)
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

            //HwitSetIOValue(5, 0)
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