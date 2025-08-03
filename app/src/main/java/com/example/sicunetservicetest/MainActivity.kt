package com.example.sicunetservicetest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sicunetservicetest.databinding.ActivityMainBinding
import android.provider.Settings
import com.hwit.HwitManager
import com.peripheral.library.PhController
import java.net.NetworkInterface

//changed

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var wifiManager: WifiManager

    private var tag = "MainActivity"

    private var value = 0

    private val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100

    private val MY_PERMISSIONS_WRITE_SETTINGS = 101

    private fun enableWifiPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
            ),
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
        )
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.WRITE_SETTINGS
            ),
            MY_PERMISSIONS_WRITE_SETTINGS
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            var allGranted = true
            for(item in grantResults){
                if(item != PackageManager.PERMISSION_GRANTED){
                    allGranted = false
                    break
                }
            }
            if(allGranted) scanWifiNetworks(permissions)
            else { Toast.makeText(this, "Location permission is required to scan Wi-Fi networks", Toast.LENGTH_SHORT).show() }
        }
        else if(requestCode == MY_PERMISSIONS_WRITE_SETTINGS){
            requestWriteSettingsPermission(this)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun scanWifiNetworks(permissions: Array<String?>) {
        for(currentPermission in permissions){
            if(currentPermission == null) return
            else {
                if (ContextCompat.checkSelfPermission(this, currentPermission) != PackageManager.PERMISSION_GRANTED){
                    return
                }
            }
        }

        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val scanResults: List<ScanResult> = wifiManager.scanResults

        for (scanResult in scanResults) {
            var ssid = ""
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val byteArray = scanResult.wifiSsid?.bytes
                byteArray?.let {
                    ssid = String(byteArray, Charsets.UTF_8)
                }
            }
            else { ssid = scanResult.SSID }

            Log.d("SSID", "SSID: $ssid BSID: ${scanResult.BSSID} LEVEL: ${scanResult.level}")
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonStartBle.setOnClickListener {
            //connectToWifi("Sicunet 5G", "sicunet2025")
//            if(!Settings.System.canWrite(this)){
//                requestWriteSettingsPermission(this)
//            }
//            else{
//                conToWifi2()
//            }
            //HwitManager.HwitSetIOValue(5, 1)

//            HwitManager.HwitSetWifiStaticIpConnect(
//                this,
//                "Sicunet 5G",
//                "sicunet2025",
//                2,
//                "192.168.1.203",
//                "192.168.1.1",
//                "255.255.255.0",
//                "8.8.8.8",
//                "8.8.4.4"
//            )

//            HwitManager.HwitSetLocalIP(
//                this,
//                "192.168.1.50",
//                "eth0",
//                0
//            )
//            HwitManager.HwitSetWifiDhcpIpConnect(
//                this,
//                "Sicunet 5G",
//                "sicunet2025",
//                2,
//            )

            PhController.whiteLight_Control_Open(this)
            PhController.green_Led_Open()

            //PhController.relay_Control_Open()

            //PhController.reboot(this)

        }
        binding.buttonStopService.setOnClickListener {
            //HwitManager.HwitSetIOValue(5, 0)
            //adb command: adb shell ifconfig eth0

            //HwitManager.HwitSetDhcpIp(this)

//            HwitManager.HwitSetStaticIp(
//                this,
//                "192.168.1.52",
//                "192.168.1.1",
//                "255.255.255.0",
//                "8.8.8.8",
//                "8.8.4.4",
//            )

            PhController.whiteLight_Control_Close(this)

            PhController.close_Led()

            //PhController.relay_Control_Close()
        }

        binding.timerService.text = getLocalIpAddress() ?: "NO IP FOUND"
        //enableWifiPermission()
    }

    private fun requestWriteSettingsPermission(context: Context) {
        if (!Settings.System.canWrite(context)) {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:" + context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Toast.makeText(context, "Please allow modifying system settings", Toast.LENGTH_LONG).show()
        }
    }

    private fun connectToWifi(ssid: String, password: String) {
        val wifiConnector = WifiConnector(applicationContext)
        wifiConnector.connectToWifi(ssid, password)
    }

    class WifiConnector(private val context: Context) {

        fun connectToWifi(ssid: String, password: String) {
            val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager

            // Create a Wi-Fi network suggestion
            val suggestion = WifiNetworkSuggestion.Builder()
                .setSsid(ssid) // SSID of the network
                .setWpa2Passphrase(password) // Password for WPA2 networks
                .setIsAppInteractionRequired(true) // Requires user interaction to connect
                .build()

            // Add the suggestion to the Wi-Fi manager
            val suggestionsList = listOf(suggestion)
            val status = wifiManager.addNetworkSuggestions(suggestionsList)


            if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                // Successfully added the suggestion
                // The system will automatically attempt to connect to the network
            } else {
                // Failed to add the suggestion
            }
        }
    }

    private fun conToWifi2(){
        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid("Sicunet 5G")
            .setWpa2Passphrase("sicunet2025")
            .build()

        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()

        val connectivityManager = applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                Log.d("Network work", "on Available: called")
                connectivityManager.bindProcessToNetwork(network)
            }

            override fun onUnavailable() {
                Log.d("Network work", "on unavailable: called")
            }
        }

        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address.hostAddress != null) {
                        // Check if it's an IPv4 address
                        if (address.hostAddress.indexOf(':') < 0) {
                            return address.hostAddress
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

}