package com.example.sicunetservicetest

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dk.uartnfc.DeviceManager.UartNfcDevice
import com.example.sicunetservicetest.databinding.ActivityMainBinding
import com.sdk.api.manager.ApiManager
import java.net.Inet4Address

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var tag = "MainActivity"

    private var value = 0

    val PERMISSION_REQUEST_CODE: Int = 1

    private var apiManager: ApiManager? = null

    private var uartNfcDevice: UartNfcDevice? = null


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestPermission()
        //apiManager = ApiManager.getInstance(this)

        binding.buttonStartBle.setOnClickListener {
//            val info = getLocalIpAddress(this)
//            Log.d("AppActivity", "onCreate: $info")
            TwilioCallManager.call(
                this,
                "",
                "+8801745327032",
                { Log.d(tag, "onRinging") },
                { Log.d(tag, "onConnected") },
                { Log.d(tag, "onDisconnected") },
                { Log.d(tag, "onError") },
            )
        }
        binding.buttonStopService.setOnClickListener {
           TwilioCallManager.hangUp()
        }
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.RECORD_AUDIO,
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            var allGranted = true
            for(item in grantResults){
                if(item != PackageManager.PERMISSION_GRANTED){
                    allGranted = false
                    break
                }
            }
            Toast.makeText(this, "All granted: $allGranted", Toast.LENGTH_SHORT).show()
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getLocalIpAddress(context: Context): MutableMap<String, String> {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val linkProperties = cm.getLinkProperties(cm.activeNetwork)

            // ── IP Address ───────────────────────────────────────────────────
            val address = linkProperties?.linkAddresses
                ?.firstOrNull { it.address is Inet4Address && !it.address.isLoopbackAddress }

            val ipAddress = address?.address?.hostAddress ?: "Not found"

            // ── Subnet ───────────────────────────────────────────────────────
            val subnet = address
                ?.let { prefixLengthToSubnetMask(it.prefixLength) }
                ?: "Not found"

            // ── Gateway ──────────────────────────────────────────────────────
            val gateway = linkProperties?.routes
                ?.firstOrNull { it.isDefaultRoute && it.gateway != null }
                ?.gateway?.hostAddress ?: "Not found"

            // ── DNS ──────────────────────────────────────────────────────────
            val dns1 = linkProperties?.dnsServers?.getOrNull(0)?.hostAddress ?: "Not found"
            val dns2 = linkProperties?.dnsServers?.getOrNull(1)?.hostAddress ?: "Not found"

            // ── Interface Name ───────────────────────────────────────────────
            val interfaceName = linkProperties?.interfaceName ?: "unknown"

            return mutableMapOf<String, String>().apply {
                put("address", ipAddress)
                put("via",     interfaceName)
                put("subnet",  subnet)
                put("gateway", gateway)
                put("dns1",    dns1)
                put("dns2",    dns2)
            }

        } catch (ex: Exception) {
            Log.d("NetworkInfo", "exception: ${ex.printStackTrace()}")
            return mutableMapOf<String, String>().apply {
                put("address", "Exception occurs")
                put("via",     "Exception occurs")
                put("subnet",  "Exception occurs")
                put("gateway", "Exception occurs")
                put("dns1",    "Exception occurs")
                put("dns2",    "Exception occurs")
            }
        }
    }

    private fun prefixLengthToSubnetMask(prefixLength: Int): String {
        val mask = if (prefixLength == 0) 0 else (-1 shl (32 - prefixLength))
        return "${(mask shr 24) and 0xFF}.${(mask shr 16) and 0xFF}.${(mask shr 8) and 0xFF}.${mask and 0xFF}"
    }
}