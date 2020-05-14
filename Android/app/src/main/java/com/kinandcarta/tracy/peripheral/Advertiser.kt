package com.kinandcarta.tracy.peripheral

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import android.util.Log
import com.kinandcarta.tracy.serviceUUID

class Advertiser {

    companion object {
        private const val tag = "Tracy - Broadcaster"
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            Log.e(tag, "Advertising failed with error code: $errorCode")
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(tag, "Advertising started successfully")
        }
    }

    fun startAdvertising() {
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setConnectable(true)
            .setTimeout(0)
            .build()
        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(serviceUUID))
            .build()
        BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser.startAdvertising(settings, data, advertiseCallback)
    }

    fun stopAdvertising() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
    }

}