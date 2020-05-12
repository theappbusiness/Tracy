package com.kinandcarta.tracy

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.ParcelUuid

class Central {

    private val adapter = BluetoothAdapter.getDefaultAdapter()
    private val scanner = adapter.bluetoothLeScanner
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
            TODO("Implement")
        }

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            TODO("Implement")
        }
    }

    fun startScanningForPeripherals() { // TODO: Request location permissions
        scanner.startScan(listOf(filter), settings, scanCallback)
    }

    fun stopScanningForPeripherals() {
        scanner.stopScan(scanCallback)
    }

}