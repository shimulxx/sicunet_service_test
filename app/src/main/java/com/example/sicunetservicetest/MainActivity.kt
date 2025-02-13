package com.example.sicunetservicetest
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sicunetservicetest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var wifiManager: WifiManager

    private var tag = "MainActivity"

    private var value = 0

    private val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100

    private fun enableWifiPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
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

        }
        binding.buttonStopService.setOnClickListener {

        }
        enableWifiPermission()
    }

}