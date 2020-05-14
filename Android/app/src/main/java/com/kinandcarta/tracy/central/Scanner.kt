package com.kinandcarta.tracy.central

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.util.Log
import com.kinandcarta.tracy.serviceUUID

/**
 * A Scanner is responsible for searching for / discovering nearby Peripherals.
 * Once a Peripheral is discovered you can use a Connector to connect to it and
 * request data.
 *
 * @see Connector
 */
class Scanner {

    companion object {
        private const val tag = "Tracy - Scanner"
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanFailed(errorCode: Int) {
            Log.e(tag, "Scan failed with error code: $errorCode")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.v(tag, "Scan result with callback type: $callbackType")
            if (result == null) {
                Log.e(tag, "No ScanResult, cannot process!")
                return
            }
            if (!discoveries.add(result.device)) return
            Log.d(tag, "Discovered new device. Name: ${result.device.name}, RSSI: ${result.rssi}, Address: ${result.device.address}")
            onDiscoveredDevice(result.device)
        }
    }
    private lateinit var onDiscoveredDevice: (BluetoothDevice) -> Unit
    private val discoveries = mutableSetOf<BluetoothDevice>()

    /**
     * Starts scanning for nearby devices.
     * Upon discovery, the onDiscoveredDevice handler is called with the device.
     * Each discovery is reported exactly once.
     */
    fun startScanning(onDiscoveredDevice: (BluetoothDevice) -> Unit) {
        this.onDiscoveredDevice = onDiscoveredDevice
        val filter = ScanFilter.Builder()
            .setServiceUuid(ParcelUuid(serviceUUID))
            .build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .setMatchMode(ScanSettings.MATCH_MODE_STICKY)
            .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
            .setReportDelay(0)
            .build()
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner.startScan(listOf(filter), settings, scanCallback)
    }

    /**
     * Stops scanning for nearby Peripherals.
     * Calling this and then starting scanning will cause any previously
     * discovered Peripherals to be reported again (if they're still nearby).
     */
    fun stopScanning() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner.stopScan(scanCallback)
        discoveries.clear()
    }

}