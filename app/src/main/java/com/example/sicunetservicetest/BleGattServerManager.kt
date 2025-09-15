package com.example.sicunetservicetest

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.bluetooth.le.BluetoothLeAdvertiser
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import java.util.*

class BleGattServerManager(private val context: Context) {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothLeAdvertiser: BluetoothLeAdvertiser? = null
    private var bluetoothGattServer: BluetoothGattServer? = null
    private var bluetoothManager: BluetoothManager? = null
    private var isAdvertising = false

    // Data received callback
    var onDataReceived: ((device: BluetoothDevice, data: ByteArray) -> Unit)? = null
    var onDeviceConnected: ((device: BluetoothDevice) -> Unit)? = null
    var onDeviceDisconnected: ((device: BluetoothDevice) -> Unit)? = null

    companion object {
        private const val TAG = "BleGattServer"

        // Custom service and characteristic UUIDs
        val SERVICE_UUID: UUID = UUID.fromString("12345678-1234-5678-9012-123456789abc")
        val WRITE_CHARACTERISTIC_UUID: UUID = UUID.fromString("12345678-1234-5678-9012-123456789abd")
        val READ_CHARACTERISTIC_UUID: UUID = UUID.fromString("12345678-1234-5678-9012-123456789abe")
        val NOTIFY_CHARACTERISTIC_UUID: UUID = UUID.fromString("12345678-1234-5678-9012-123456789abf")
    }

    init {
        bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager?.adapter
        bluetoothLeAdvertiser = bluetoothAdapter?.bluetoothLeAdvertiser
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun startServer() {
        if (!checkPermissions()) {
            Log.e(TAG, "Missing required permissions")
            return
        }

        if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
            Log.e(TAG, "Bluetooth is not enabled")
            return
        }

        setupGattServer()
        startAdvertising()
    }

    @RequiresPermission(allOf = [
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_ADVERTISE,
    ])
    fun stopServer() {
        stopAdvertising()
        bluetoothGattServer?.close()
        bluetoothGattServer = null
        Log.i(TAG, "GATT Server stopped")
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    private fun setupGattServer() {
        bluetoothGattServer = bluetoothManager?.openGattServer(context, gattServerCallback)

        // Create the service
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)

        // Create characteristics
        val writeCharacteristic = BluetoothGattCharacteristic(
            WRITE_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE or BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )

        val readCharacteristic = BluetoothGattCharacteristic(
            READ_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )

        val notifyCharacteristic = BluetoothGattCharacteristic(
            NOTIFY_CHARACTERISTIC_UUID,
            BluetoothGattCharacteristic.PROPERTY_NOTIFY,
            BluetoothGattCharacteristic.PERMISSION_READ
        )


        // Add descriptor for notifications
        val notifyDescriptor = BluetoothGattDescriptor(
            UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"), // Client Characteristic Configuration
            BluetoothGattDescriptor.PERMISSION_READ or BluetoothGattDescriptor.PERMISSION_WRITE
        )
        notifyCharacteristic.addDescriptor(notifyDescriptor)

        // Add characteristics to service
        service.addCharacteristic(writeCharacteristic)
        service.addCharacteristic(readCharacteristic)
        service.addCharacteristic(notifyCharacteristic)

        // Add service to server
        bluetoothGattServer?.addService(service)

        Log.i(TAG, "GATT Server setup completed")
    }

    private var receivedData: ByteArray = byteArrayOf()

    private val gattServerCallback = object : BluetoothGattServerCallback() {

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)

            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    Log.i(TAG, "Device connected: ${device?.address}")
                    device?.let { onDeviceConnected?.invoke(it) }
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    Log.i(TAG, "Device disconnected: ${device?.address}")
                    device?.let { onDeviceDisconnected?.invoke(it) }
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)

            when (characteristic?.uuid) {
                WRITE_CHARACTERISTIC_UUID -> {
                    value?.let { data ->
                        receivedData = data
                        Log.i(TAG, "Data received from ${device?.address}: ${data.contentToString()}")
                        Log.i(TAG, "Data as string: ${String(data)}")

                        device?.let { dev ->
                            onDataReceived?.invoke(dev, data)
                        }
                    }

                    if (responseNeeded) {
                        bluetoothGattServer?.sendResponse(
                            device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value
                        )
                    }
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onCharacteristicReadRequest(device: BluetoothDevice?, requestId: Int, offset: Int, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicReadRequest(device, requestId, offset, characteristic)

            when (characteristic?.uuid) {
                READ_CHARACTERISTIC_UUID -> {
                    val response = "Hello from server ${String(receivedData)}".toByteArray()
                    bluetoothGattServer?.sendResponse(
                        device, requestId, BluetoothGatt.GATT_SUCCESS, offset, response
                    )
                    Log.i(TAG, "Read request from ${device?.address}")
                }
            }
        }

        @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
        override fun onDescriptorWriteRequest(
            device: BluetoothDevice?,
            requestId: Int,
            descriptor: BluetoothGattDescriptor?,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value)

            if (responseNeeded) {
                bluetoothGattServer?.sendResponse(
                    device, requestId, BluetoothGatt.GATT_SUCCESS, offset, value
                )
            }

            Log.i(TAG, "Descriptor write request from ${device?.address}")
        }

        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            super.onServiceAdded(status, service)
            Log.i(TAG, "Service added with status: $status")
        }
    }

    private fun startAdvertising() {
        if (bluetoothLeAdvertiser == null) {
            Log.e(TAG, "BLE advertising not supported")
            return
        }

        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(true)
            .setTimeout(0)
            .build()

        val data = AdvertiseData.Builder()
            .setIncludeDeviceName(false)
            .setIncludeTxPowerLevel(false)
            .addManufacturerData(0x004C, byteArrayOf(0x01, 0x02, 0x03))
            .build()

        val scanResponse = AdvertiseData.Builder()
            .setIncludeDeviceName(true)
            .addServiceUuid(android.os.ParcelUuid(SERVICE_UUID))
            .build()

        bluetoothLeAdvertiser!!.startAdvertising(settings, data, scanResponse, advertiseCallback)
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_ADVERTISE)
    private fun stopAdvertising() {
        bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        isAdvertising = false
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            super.onStartSuccess(settingsInEffect)
            isAdvertising = true
            Log.i(TAG, "Advertising started successfully")
        }

        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            isAdvertising = false
            Log.e(TAG, "Advertising failed with error: $errorCode")
        }
    }

    // Send notification to connected device
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun sendNotification(device: BluetoothDevice, data: ByteArray): Boolean {
        val service = bluetoothGattServer?.getService(SERVICE_UUID)
        val characteristic = service?.getCharacteristic(NOTIFY_CHARACTERISTIC_UUID)

        characteristic?.value = data

        return bluetoothGattServer?.notifyCharacteristicChanged(device, characteristic, false) ?: false
    }

    // Send notification to all connected devices
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun broadcastNotification(data: ByteArray) {
        bluetoothManager?.getConnectedDevices(BluetoothProfile.GATT_SERVER)?.forEach { device ->
            sendNotification(device, data)
        }
    }

    private fun checkPermissions(): Boolean {
        val permissions = mutableListOf<String>()

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH)
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
        }

        return permissions.isEmpty()
    }
}