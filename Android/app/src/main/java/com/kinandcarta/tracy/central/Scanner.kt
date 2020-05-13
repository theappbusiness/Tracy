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

class Scanner(
    private val onDiscoveredDevice: (BluetoothDevice) -> Unit
) {

    companion object {
        private const val tag = "Tracy - Scanner"
    }

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
            if (!discoveries.add(result.device)) return
            Log.d(tag, "Discovered new device. Name: ${result.device.name}, RSSI: ${result.rssi}, Address: ${result.device.address}")
            onDiscoveredDevice(result.device)
        }
    }
    private val discoveries = mutableSetOf<BluetoothDevice>()

    fun startScanning() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner.startScan(listOf(filter), settings, scanCallback)
    }

    fun stopScanning() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner.stopScan(scanCallback)
        discoveries.clear()
    }

}