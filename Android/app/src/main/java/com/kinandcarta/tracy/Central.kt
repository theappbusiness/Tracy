package com.kinandcarta.tracy

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid
import android.util.Log

class Central {

    companion object {
        private const val tag = "Tracy - Central"
    }

    private val scanner =  BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private val filter = ScanFilter.Builder()
        .setServiceUuid(ParcelUuid(serviceUUID))
        .build()
    private val settings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
        .setReportDelay(0)
        .build()
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
            if (!discoveries.add(result.device)) {
                Log.v(tag, "Already discovered device, ignoring: ${result.device.address}")
                return
            }
            Log.d(tag, "Discovered new device. Name: ${result.device.name}, RSSI: ${result.rssi}, Address: ${result.device.address}")
            // This is where you'd connect to the discovered device (peripheral) and transfer whatever data you needed to identify it as a trace
        }
    }
    private val discoveries = mutableSetOf<BluetoothDevice>()

    fun startScanningForPeripherals() { // TODO: Request location permissions
        scanner.startScan(listOf(filter), settings, scanCallback)
    }

    fun stopScanningForPeripherals() {
        scanner.stopScan(scanCallback)
    }

}