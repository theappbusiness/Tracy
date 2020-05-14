package com.kinandcarta.tracy.peripheral

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.os.ParcelUuid
import android.util.Log
import com.kinandcarta.tracy.serviceUUID

/**
 * An Advertiser advertises this device as a Peripheral to be discovered and connected to by other Centrals.
 * Connections are handled by Servers.
 * @see Server
 */
class Advertiser {

    companion object {
        private const val tag = "Tracy - Advertiser"
    }

    private val advertiseCallback = object : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            Log.e(tag, "Advertising failed with error code: $errorCode")
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings?) {
            Log.d(tag, "Advertising started successfully")
            onStarted()
        }
    }

    lateinit var onStarted: () -> Unit

    /**
     * Starts advertising this device as a Peripheral to be discovered by other Centrals.
     */
    fun startAdvertising(onStarted: () -> Unit) {
        this.onStarted = onStarted
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

    /**
     * Stops advertising this device as a Peripheral so it cannot be discovered by other Centrals.
     */
    fun stopAdvertising() {
        BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser.stopAdvertising(advertiseCallback)
    }

}