package com.kinandcarta.tracy

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import android.util.Log

class Peripheral {

    companion object {
        private const val tag = "Tracy - Peripheral"
    }

    private val scanner =  BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
    private val settings = AdvertiseSettings.Builder()
        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
        .setConnectable(true)
        .setTimeout(0)
        .build()
    private val data = AdvertiseData.Builder()
        .addServiceUuid(ParcelUuid(serviceUUID))
        .build()
    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            Log.e(tag, "Advertising failed with error code: $errorCode")
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.e(tag, "Advertising started successfully")
        }
    }

    fun startAdvertisingToCentrals() {
        scanner.startAdvertising(settings, data, advertiseCallback)
    }

    fun stopAdvertisingToCentrals() {
        scanner.stopAdvertising(advertiseCallback)
    }

}