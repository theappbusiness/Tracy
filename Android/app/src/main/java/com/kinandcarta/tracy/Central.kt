package com.kinandcarta.tracy

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log

class Central {

    companion object {
        private const val tag = "Tracy - Central"
    }

    private lateinit var context: Context
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
            val gatt = result.device.connectGatt(context, false, connectionCallback)
        }
    }
    private val connectionCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.v(tag, "Connection state changed for ${gatt?.device?.address} to $newState with status $status")
            when (Pair(status, newState)) {
                Pair(BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING) ->
                    Log.d(tag, "Connecting to ${gatt?.device?.address}")
                Pair(BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED) ->
                    Log.d(tag, "Connected to ${gatt?.device?.address}")
                Pair(BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTING) ->
                    Log.d(tag, "Disconnecting from ${gatt?.device?.address}")
                Pair(BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_DISCONNECTED) ->
                    Log.d(tag, "Disconnected from ${gatt?.device?.address}")
                else -> Log.e(tag, "Error connecting or disconnecting from ${gatt?.device?.address} with status $status and state $newState")
            }
        }
    }
    private val discoveries = mutableSetOf<BluetoothDevice>()
    private val connections = mutableSetOf<BluetoothGatt>()

    fun startScanningForPeripherals(context: Context) {
        this.context = context
        scanner.startScan(listOf(filter), settings, scanCallback)
    }

    fun stopScanningForPeripherals() {
        scanner.stopScan(scanCallback)
        discoveries.clear()
        connections.forEach {
            it.disconnect()
            it.close()
        }
        connections.clear()
    }

}