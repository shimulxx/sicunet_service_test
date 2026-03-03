package com.example.sicunetservicetest
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.dk.uartnfc.DeviceManager.UartNfcDevice
import com.example.sicunetservicetest.databinding.ActivityMainBinding
import com.sdk.api.manager.ApiManager
import java.net.Inet4Address
import java.net.NetworkInterface

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var tag = "MainActivity"

    private var value = 0

    val PERMISSION_REQUEST_CODE: Int = 1


    private var apiManager: ApiManager? = null

    private var uartNfcDevice: UartNfcDevice? = null

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
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
        setContentView(binding.root)
        requestPermission()
        //apiManager = ApiManager.getInstance(this)

        binding.buttonStartBle.setOnClickListener {
            val info = getLocalIpAddress(this)
            Log.d("AppActivity", "onCreate: $info")
        }
        binding.buttonStopService.setOnClickListener {

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
                Manifest.permission.ACCESS_NETWORK_STATE
            ),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun getLocalIpAddress(context: Context): MutableMap<String, String> {
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val linkProperties = cm.getLinkProperties(cm.activeNetwork)
            val capabilities   = cm.getNetworkCapabilities(cm.activeNetwork)

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

            // ── Connection Type ──────────────────────────────────────────────
            val connectionType = when {
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> "LAN (Ethernet)"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)     == true -> "WiFi"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Cellular"
                capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN)      == true -> "VPN"
                else -> "Unknown"
            }

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

//    private fun getLocalIpAddress(): MutableMap<String, String> {
//        try {
//            val interfaces = NetworkInterface.getNetworkInterfaces()
//            while (interfaces.hasMoreElements()) {
//                val networkInterface = interfaces.nextElement()
//                val addresses = networkInterface.inetAddresses
//                while (addresses.hasMoreElements()) {
//                    val address = addresses.nextElement()
//                    if (!address.isLoopbackAddress && address.hostAddress != null) {
//                        if (address.hostAddress.indexOf(':') < 0) {
//                            return mutableMapOf<String, String>().apply {
//                                put("address", address.hostAddress ?: "")
//                                put("via", networkInterface.displayName ?: "")
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        catch (ex: Exception) {
//            Log.d("exception is", "exception: ${ex.printStackTrace()}")
//            return mutableMapOf<String, String>().apply {
//                put("address", "Exception occurs")
//                put("via", "Exception occurs")
//            }
//        }
//        return mutableMapOf<String, String>().apply {
//            put("address", "Not found")
//            put("via", "Not found")
//        }
//    }
}