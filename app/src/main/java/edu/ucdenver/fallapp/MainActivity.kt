package edu.ucdenver.fallapp

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.BluetoothAdapter.ACTION_REQUEST_ENABLE
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.bluetooth.le.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import edu.ucdenver.fallapp.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.list_item_scan.*
import java.util.*
import java.util.jar.Manifest

private const val TAG = "MainAct"

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class MainActivity : AppCompatActivity() {

    /*** PROPERTIES ***/

    private lateinit var binding: ActivityMainBinding

    private lateinit var scanResultList: RecyclerView
    private lateinit var scanResultAdapter: ScanResultAdapter

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val isLocationPermissionGranted
        get() = hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)


/*    val device_name = "ScioMed_G4"
    val filters: List<ScanFilter> = emptyList()
    val nameFilter = ScanFilter.Builder().setDeviceName(device_name).build()*/

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
            runOnUiThread{ scan_button.text = if (value) "Stop Scan" else "Start Scan"}
            // Toast.makeText(this,if (field) "Stopped scan" else "Started scanning",Toast.LENGTH_SHORT)
        }

    private var devicesFound = false
        set(value) {
            field = value
            runOnUiThread { connect_button.text = if (value) "Connect to Device" else "No Devices Found" }
        }

    private val scanResults = mutableListOf<ScanResult>()
/*    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) {
            // TODO: Implement
        }
    }*/

    fun ByteArray.toHexString() : String =
        joinToString(separator = " ", prefix = "0x") { String.format("%02X",it) }



    /*** OVERRIDES ***/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        scan_button.setOnClickListener {
            Log.d(TAG,"BeforeScanButton")
            if (isScanning) {
                Log.d(TAG,"StopScan")
                stopBleScan()
            } else {
                Log.d(TAG,"StartScan")
                startBleScan()

                devicesFound = true
            }
            Log.d(TAG,"AfterScanButton")
        }

        connect_button.setOnClickListener {
            if (scanResults.any()) {
                Log.d(TAG,"Connection attempt")
                stopBleScan()
                scanResults[0].device.connectGatt(this,false,gattCallback)
                Log.d(TAG,"After connectGatt")
            }
        }

        check_battery.setOnClickListener {

        }

        check_temperature.setOnClickListener {

        }



/*        binding.scanResultRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.scanResultRecyclerView.setHasFixedSize(true)

        // setUpRecyclerView()
        scan_result_recycler_view.setOnClickListener {
            Log.d(TAG,"recyclerClick")
        }*/
    }



    override fun onResume() {
        super.onResume()
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
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





    /*** PRIVATE FUNCTIONS ***/
    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

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
        Log.d(TAG,"Permission fail")
        /*runOnUiThread {
            alert {
                title = "Location permission required"
                message = "Starting from Android M (6.0), the system requires apps to be granted" +
                        "location access in order to scan for BLE devices."
                isCancelable = false
                positiveButton(android.R.string.ok) {
                    requestPermission(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }.show()
        }*/
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





/*
    private fun setUpRecyclerView() {
        scan_result_recycler_view.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@MainActivity,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = scan_result_recycler_view.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }
*/


    /*** CALLBACK BODIES ***/

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

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val deviceAddress = gatt.device.address

            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.w("BluetoothGattCallback", "Successfully connected to $deviceAddress")
                    // TODO: Store a reference to BluetoothGatt
                    val bluetoothGatt = gatt
                    gatt.discoverServices()
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
            runOnUiThread { connect_button.text = "Device Connected" }
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


    }



    /*** EXTENSION FUNCTIONS ***/
    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this,permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }


}
