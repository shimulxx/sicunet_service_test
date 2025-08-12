package com.example.sicunetservicetest
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
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
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.sicunetservicetest.databinding.ActivityMainBinding
import android.provider.Settings
import androidx.annotation.RequiresPermission
import com.dk.uartnfc.DKCloudID.IDCardData
import com.dk.uartnfc.DeviceManager.DeviceManagerCallback
import com.dk.uartnfc.DeviceManager.UartNfcDevice
import com.hwit.HwitManager
import com.peripheral.library.PhController
import com.sdk.api.manager.ApiManager
import java.net.NetworkInterface
import java.util.UUID
import kotlin.math.log

//changed

fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }.uppercase()

class MainActivity : AppCompatActivity() {

    external fun stringFromJNI(): String

    companion object {
        // Used to load the 'myndktest' library on application startup.
        init {
            System.loadLibrary("sicunetservicetest")
        }
    }

    private lateinit var binding: ActivityMainBinding

    private lateinit var wifiManager: WifiManager

    private var tag = "MainActivity"

    private var value = 0

    private val MY_PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100

    private val MY_PERMISSIONS_WRITE_SETTINGS = 101

    private lateinit var apiManger: ApiManager

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

    private fun initWeigonListener(){
        PhController.weigen26Write("test",object: PhController.WeigenResultListener{
            override fun onWriteInfo(p0: String?) {
                Log.d("CARD INFO", "onWriteInfo: $p0")
            }

        })

        PhController.wiegand34Write("hello", object: PhController.WeigenResultListener{
            override fun onWriteInfo(p0: String?) {
                Log.d("CARD INFO", "onWriteInfo: $p0")
            }

        })
    }

    var uartNfcDevice: UartNfcDevice? = null
    private fun initReader(){
        uartNfcDevice = UartNfcDevice()
        val ports = uartNfcDevice?.serialManager?.availablePorts
        //uartNfcDevice?.serialManager?.open("/dev/tty/ttyS4", "115200")
        //uartNfcDevice?.openDevice("ttyS4")
        if(uartNfcDevice?.serialManager?.isOpen ?: false) uartNfcDevice?.serialManager?.close()
        uartNfcDevice?.serialManager?.open("/dev/ttyS4", "115200")
        uartNfcDevice?.setCallBack(object: DeviceManagerCallback() {
            override fun onReceiveRfnSearchCard(blnIsSus: Boolean, cardType: Int, bytCardSn: ByteArray?, bytCarATS: ByteArray?) {
                super.onReceiveRfnSearchCard(blnIsSus, cardType, bytCardSn, bytCarATS)
                val cardSerialHex = bytCardSn?.toHexString()
                Log.d("CARD WORK", "TYPE: $cardType")
                Log.d("CARD WORK", "Serial: $cardSerialHex")
                Log.d("CARD WORK", "ATS: ${bytCarATS?.toHexString()}")

                val facility = cardSerialHex?.take(2)
                val serialNumber = cardSerialHex?.drop(2)

                Log.d("CARD WORK", "Facility Code: ${facility?.toInt(16)}")
                Log.d("CARD WORK", "Serial Number: ${serialNumber?.toInt(16)}")
            }

            override fun onReceiveSamVIdStart(initData: ByteArray?) {
                Log.d("CARD WORK", "onReceiveSamVIdStart()")
                super.onReceiveSamVIdStart(initData)
            }

            override fun onReceiveSamVIdSchedule(rate: Int) {
                Log.d("CARD WORK", "onReceiveSamVIdSchedule()")
                super.onReceiveSamVIdSchedule(rate)
            }

            override fun onReceiveSamVIdException(msg: String?) {
                Log.d("CARD WORK", "onReceiveSamVIdException()")
                super.onReceiveSamVIdException(msg)
            }

            override fun onReceiveIDCardData(idCardData: IDCardData?) {
                Log.d("CARD WORK", "onReceiveIDCardData()")
                super.onReceiveIDCardData(idCardData)
            }

            override fun onReceiveCardLeave() {
                Log.d("CARD WORK", "onReceiveCardLeave()")
                super.onReceiveCardLeave()
            }

            override fun onReceiveACK() {
                Log.d("CARD WORK", "onReceiveACK()")
                super.onReceiveACK()
            }

            override fun onReceiveNACK() {
                Log.d("CARD WORK", "onReceiveNACK()")
                super.onReceiveNACK()
            }
        })
    }

    override fun onDestroy() {
        uartNfcDevice?.closeDevice()
        uartNfcDevice?.destroy()
        uartNfcDevice?.release()
        super.onDestroy()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        apiManger = ApiManager.getInstance(this)
        //initWeigonListener()
        initReader()
//        requestPermissions(
//            arrayOf(
//                Manifest.permission.BLUETOOTH_ADVERTISE,
//            ),
//            577
//        )
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

//            PhController.whiteLight_Control_Open(this)
//            PhController.green_Led_Open()

            //PhController.relay_Control_Open()

            //PhController.reboot(this)

//            PhController.showStatusBar(this)
//            PhController.showNavigationBar(this)

            //PhController.doorbell_control_open()

            //testBLEAdvertise()

            //Log.d("CARD INFO", "onCreate: ${PhController.weigen26Read()}")
            //testBLEAdvertise()

            Log.d("MainActivity Work", "${apiManger.runningMemory}")

//            apiManger.setStaticIpMode(
//                "192.168.1.20",
//                "255.255.255.0",
//                "192.168.1.1",
//                "8.8.8.8",
//                "8.8.4.4"
//            )

            apiManger.setStaticIpMode(
                "192.168.1.248",
                "255.255.255.0",
                "192.168.1.1",
                "8.8.8.8",
                "8.8.4.4"
            )

//            apiManger.setWifiStaticIpMode(
//                "192.168.1.248",
//                24,
//                "192.168.1.1",
//                "8.8.8.8",
//                "8.8.4.4"
//            )

            //apiManger.setWifiDhcpMode()
            //apiManger.setEthDhcpMode()

        }
//        binding.buttonStopService.setOnClickListener {
//            //HwitManager.HwitSetIOValue(5, 0)
//            //adb command: adb shell ifconfig eth0
//
//            //HwitManager.HwitSetDhcpIp(this)
//
////            HwitManager.HwitSetStaticIp(
////                this,
////                "192.168.1.52",
////                "192.168.1.1",
////                "255.255.255.0",
////                "8.8.8.8",
////                "8.8.4.4",
////            )
//
////            PhController.whiteLight_Control_Close(this)
////
////            PhController.close_Led()
//
////            PhController.hideNavigationBar(this)
////            PhController.hideStatusBar(this)
//
//            //PhController.relay_Control_Close()
//
//           // PhController.doorbell_control_close()
//            bluetoothLeAdvertiser?.stopAdvertising(callback)
//        }

        //binding.timerService.text = getLocalIpAddress() ?: "NO IP FOUND"
        binding.timerService.text = stringFromJNI()
        //enableWifiPermission()

    }

    var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null

    val callback = object : AdvertiseCallback(){
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d("BLE WORK", "onStartSuccess: ")
            super.onStartSuccess(settingsInEffect)
        }

        override fun onStartFailure(errorCode: Int) {
            Log.d("BLE WORK", "onStartFailure: ")
            super.onStartFailure(errorCode)
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    private fun testBLEAdvertise(){

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

       // bluetoothAdapter.name = "BD"

        //NOT COMPLETED YET, NEED FURTHER DISCUSSION.

        // Check if advertising is supported
//        if (!bluetoothAdapter.isMultipleAdvertisementSupported) {
//            Log.e("BLE WORK", "Advertising not supported")
//            return
//        }
//        else {
//            Log.d("BLE WORK", "testBLEAdvertise: SUPPORTED") }

        bluetoothLeAdvertiser = bluetoothAdapter.bluetoothLeAdvertiser

        Log.d("BLE WORK", "ENABLED: ${bluetoothAdapter.isEnabled}")

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true) // Set to false for non-connectable advertising
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false) // Include device name
            .addServiceUuid(ParcelUuid(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))) // Example UUID
            .addServiceData(
                ParcelUuid(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")),
                "HelloWorld".toByteArray()
            )
            .build()

        bluetoothLeAdvertiser?.startAdvertising(settings, data, callback)

//        if (!bluetoothAdapter.isLeExtendedAdvertisingSupported) {
//            Log.e("BLE WORK", "Advertising not supported")
//            return
//        }
//        else {
//            Log.d("BLE WORK", "Advertising SUPPORTED") }

    }
//Bluetooth GATT SERVICE
//    private val gattServerCallback = object : BluetoothGattServerCallback() {
//        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
//            super.onConnectionStateChange(device, status, newState)
//            Log.d("BLE WORK", "Connection state changed: ${if (newState == BluetoothProfile.STATE_CONNECTED) "Connected" else "Disconnected"}")
//        }
//
//        override fun onCharacteristicReadRequest(
//            device: BluetoothDevice?,
//            requestId: Int,
//            offset: Int,
//            characteristic: BluetoothGattCharacteristic?
//        ) {
//            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)
//            // Handle read requests
//        }
//
//        override fun onCharacteristicWriteRequest(
//            device: BluetoothDevice?,
//            requestId: Int,
//            characteristic: BluetoothGattCharacteristic?,
//            preparedWrite: Boolean,
//            responseNeeded: Boolean,
//            offset: Int,
//            value: ByteArray?
//        ) {
//            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
//
//            // This is where you receive data
//            value?.let {
//                val receivedString = String(it)
//                Log.d("BLE WORK", "Received data: $receivedString")
//            }
//
//            // Send response if needed
//            if (responseNeeded) {
//                bluetoothGattServer?.sendResponse(
//                    device,
//                    requestId,
//                    BluetoothGatt.GATT_SUCCESS,
//                    0,
//                    null
//                )
//            }
//        }
//    }

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