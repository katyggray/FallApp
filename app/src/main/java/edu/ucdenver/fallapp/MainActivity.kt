package edu.ucdenver.fallapp

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.*
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import edu.ucdenver.fallapp.databinding.ActivityMainBinding
import java.util.*

private const val TAG = "MainAct"

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class MainActivity : AppCompatActivity() {

    /********************** PROPERTIES **********************/

    private lateinit var binding: ActivityMainBinding

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val isLocationPermissionGranted
        get() = hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)


    val name_filter = ScanFilter.Builder().setDeviceName("ScioMed_G4").build()
    val scanFilter = mutableListOf<ScanFilter>(name_filter)

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()


    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }


    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread{ binding.scanButton.text = if (value) "Stop Scan" else "Start Scan"}
            // Toast.makeText(this,if (field) "Stopped scan" else "Started scanning",Toast.LENGTH_SHORT)
        }

    private var devicesFound = false
        set(value) {
            field = value
            runOnUiThread { binding.connectButton.text = if (value) "Connect to Device" else "No Devices Found" }
        }

    private var deviceConnected = false

    private val scanResults = mutableListOf<ScanResult>()


    val batterServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")

    fun ByteArray.toHexString() : String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X",it) }





    /********************** METHOD OVERRIDES **********************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        requestLocationPermission()

        binding.scanButton.setOnClickListener {
            Log.d(TAG,"BeforeScanButton")
            if (isScanning) {
                Log.d(TAG,"StopScan")
                stopBleScan()
            } else {
                Log.d(TAG,"StartScan")
                startBleScan()

                Snackbar.make(binding.constraintLayout,"Bluetooth scan started",Snackbar.LENGTH_SHORT)
                    .show()

                devicesFound = true
            }
            Log.d(TAG,"AfterScanButton")
        }


        binding.connectButton.setOnClickListener {
            Log.d(TAG,"Connection attempt")
            connectDevice()

            Log.d(TAG,"After connectGatt")

        }


        binding.checkBattery.setOnClickListener {
            Log.d(TAG,"checkBattery pressed")
            checkBattery()
        }

/*        binding.checkTemperature.setOnClickListener {
            Log.d(TAG,"checkTemperature pressed")
            batteryLevel = checkBattery()
        }*/


    }


    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            val builder = AlertDialog.Builder(this)
                .setTitle("Please enable Bluetooth")
                .setMessage("Bluetooth is required for the functions of this application")
                .setCancelable(false)
                .setPositiveButton("OK",DialogInterface.OnClickListener {
                        dialog, id ->
                    startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                })

            builder.create()
            builder.show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    // promptEnableBluetooth()
                    requestLocationPermission()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }





    /********************** PRIVATE FUNCTIONS **********************/

    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        }
        else {
            scanResults.clear()
            bleScanner.startScan(scanFilter,scanSettings,scanCallback)
            isScanning = true


        }
    }

    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }


    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }

        val builder = AlertDialog.Builder(this)
            .setTitle("Please enable location access")
            .setMessage("Location access is required to establish a Bluetooth connection.\nIt is not used for any other purpose")
            .setCancelable(false)
            .setPositiveButton("OK",DialogInterface.OnClickListener {
                    dialog, id ->
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            })

        builder.create()
        builder.show()

        Log.d(TAG,"Permission fail")
    }

    private fun BluetoothGatt.printGattTable() {
        if (services.isEmpty()) {
            Log.i("printGattTable","No service and characteristic available, call discoverServices() first?")
            return
        }
        services.forEach { service ->
            val characteristicsTable = service.characteristics.joinToString(
                separator = "\n--",
                prefix = "|--"
            ) { it.uuid.toString() }
            Log.i("printGattTable","\nService ${service.uuid}\nCharacteristics:\n$characteristicsTable")
        }
    }

    private fun saveGatt(gatt: BluetoothGatt) {
        val savedGatt = gatt
    }

    private fun connectDevice() {
        Snackbar.make(binding.constraintLayout,"Starting Bluetooth connection",Snackbar.LENGTH_LONG).show()
        stopBleScan()
        var foundDevice = scanResults[0]
        val gattOut: BluetoothGatt = foundDevice.device.connectGatt(this,false,gattCallback)
        saveGatt(gattOut)
        // val gatt = scanResults[0].device.connectGatt(this,false,gattCallback)
    }

    private fun checkBattery() {
        // savedGatt
        // TODO: send readCharacteristic request, parse results, update UI with results
    }





    /********************** CALLBACK BODIES **********************/

    /*** SCAN CALLBACK ***/

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }

            if (indexQuery != -1) { // A scan result already exists with same address
                scanResults[indexQuery] = result
            } else {
                with(result!!.device) {
                    Log.i(
                        "ScanCallback",
                        "Found BLE device! Name: ${name ?: "Unnamed"}, address: $address"
                    )
                }
                scanResults.add(result)
                Log.i("ScanCallBack","Size of results: ${scanResults.size}")
            }
        }
        override fun onScanFailed(errorCode: Int) {
            Log.e("ScanCallback","onScanFailed: code $errorCode")
        }
    }



    /*** BLUETOOTH CALLBACK ***/

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    // TODO: Store a reference to BluetoothGatt

                    gatt.discoverServices()
                    saveGatt(gatt)
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully disconnected from $deviceAddress")
                    gatt.close()
                }
            } else {
                Log.w("BluetoothGattCallback", "Error $status encountered for $deviceAddress! Disconnecting...")
                gatt.close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            with(gatt) {
                Log.w("BluetoothGattCallback","Discovered ${this?.services?.size} services for ${this?.device?.address}")
                this?.printGattTable()
            }
            deviceConnected = true
            runOnUiThread { binding.connectButton.text = "Disconnect Device"
                binding.scanButton.text = "Device Currently Connected"}
            Snackbar.make(binding.constraintLayout,"Device connected",Snackbar.LENGTH_LONG).show()
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when(status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("BluetoothGattCallback","Read characteristic $uuid:\n${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_READ_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback","Read not permitted for $uuid!")
                    }
                    else -> {
                        Log.e("BluetoothGattCallback","Characteristic read failed for $uuid, error: $status")
                    }
                }
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            with(characteristic) {
                when (status) {
                    BluetoothGatt.GATT_SUCCESS -> {
                        Log.i("BluetoothGattCallback","Wrote to characteristic $uuid | value: ${value.toHexString()}")
                    }
                    BluetoothGatt.GATT_INVALID_ATTRIBUTE_LENGTH -> {
                        Log.e("BluetoothGattCallback","Write exceeded connection ATT MTU")
                    }
                    BluetoothGatt.GATT_WRITE_NOT_PERMITTED -> {
                        Log.e("BluetoothGattCallback","Write not permitted for $uuid")
                    }
                    else -> {
                        Log.e("BluetoothGattCallback","Characteristic write failed for $uuid, error: $status")
                    }
                }
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            with(characteristic) {
                Log.i("BluetoothGattCallback","Characteristic $uuid changed | value: ${value.toHexString()}")
            }
        }


    /*
        private fun readBatteryLevel() {
            val batteryServiceUuid = UUID.fromString("0000180f-0000-1000-8000-00805f9b34fb")
            val batteryLevelCharUuid = UUID.fromString("00002a19-0000-1000-8000-00805f9b34fb")
            val batteryLevelChar = gatt
                .getService(batteryServiceUuid)?.getCharacteristic(batteryLevelCharUuid)
            if (batteryLevelChar?.isReadable() == true) {
                gatt.readCharacteristic(batteryLevelChar)
            }

            val readBytes: ByteArray
            val batteryLevel = readBytes.first().toInt()
        }


        fun writeCharacteristic(characteristic: BluetoothGattCharacteristic, payload: ByteArray) {
            val writeType = when {
                characteristic.isWritable() -> BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
                characteristic.isWritableWithoutResponse() -> {
                    BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                }
                else -> error("Characteristic ${characteristic.uuid} cannot be written to")
            }

            bluetoothGatt?.let { gatt ->
                characteristic.writeType = writeType
                characteristic.value = payload
                gatt.writeCharacteristic(characteristic)
            } ?: error("Not connected to a BLE device")
        }



        fun BluetoothGattCharacteristic.isReadable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_READ)

        fun BluetoothGattCharacteristic.isWritable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE)

        fun BluetoothGattCharacteristic.isWritableWithoutResponse(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE)

        fun BluetoothGattCharacteristic.containsProperty(property: Int): Boolean {
            return properties and property != 0
        }



        fun BluetoothGattCharacteristic.isIndicatable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_INDICATE)

        fun BluetoothGattCharacteristic.isNotifiable(): Boolean =
            containsProperty(BluetoothGattCharacteristic.PROPERTY_NOTIFY)

        fun writeDescriptor(descriptor: BluetoothGattDescriptor, payload: ByteArray) {
            bluetoothGatt?.let { gatt ->
                descriptor.value = payload
                gatt.writeDescriptor(descriptor)
            } ?: error("Not connected to BLE device")
        }


        fun enableNotifications(characteristic: BluetoothGattCharacteristic) {
            val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
            val payload = when {
                characteristic.isIndicatable() -> BluetoothGattDescriptor.ENABLE_INDICATION_VALUE
                characteristic.isNotifiable() -> BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                else -> {
                    Log.e("ConnectionManager","${characteristic.uuid} doesn't support notifications/indications")
                    return
                }
            }

            characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                if (bluetoothGatt?.setCharacteristicNotification(characteristic,true) == false) {
                    Log.e("ConnectionManager","setCharacteristicNotification failed for ${characteristic.uuid}")
                    return
                }
                writeDescriptor(cccDescriptor,payload)
            } ?: Log.e("ConnectionManager","${characteristic.uuid} doesn't contain the CCC descriptor")
        }

        fun disableNotifications(characteristic: BluetoothGattCharacteristic) {
            if (!characteristic.isNotifiable() && !characteristic.isIndicatable()) {
                Log.e("ConnectionManager","${characteristic.uuid} doesn't support indications/notifications")
                return
            }

            val cccdUuid = UUID.fromString(CCC_DESCRIPTOR_UUID)
            characteristic.getDescriptor(cccdUuid)?.let { cccDescriptor ->
                if (bluetoothGatt?.setCharacteristicNotification(characteristic,false) == false) {
                    Log.e("ConnectionManager","setCharacteristicNotification failed for ${characteristic.uuid}")
                    return
                }
                writeDescriptor(cccDescriptor,BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)
            } ?: Log.e("ConnectionManager","${characteristic.uuid} doesn't contain the CCC descriptor")
        }


    */

    }







    /********************** EXTENSION FUNCTIONS **********************/
    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this,permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }


}
