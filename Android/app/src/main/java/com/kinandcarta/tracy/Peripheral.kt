package com.kinandcarta.tracy

import android.bluetooth.*
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.os.ParcelUuid
import android.util.Log

class Peripheral {

    companion object {
        private const val tag = "Tracy - Peripheral"
    }

    private val advertiser =  BluetoothAdapter.getDefaultAdapter().bluetoothLeAdvertiser
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
            Log.d(tag, "Advertising started successfully")
        }
    }
    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            Log.v(tag, "Service ${service?.uuid} added with status: $status")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(tag, "Successfully added service")
                    advertiser.startAdvertising(settings, data, advertiseCallback)
                }
                else -> Log.e(tag, "Could not add service (status code $status), advertising will not start")
            }
        }

        override fun onConnectionStateChange(device: BluetoothDevice?, status: Int, newState: Int) {
            Log.d(tag, "Connection state changed to $newState with status $status for device ${device?.address}")
        }
    }

    fun startAdvertisingToCentrals(context: Context) {
        val manager = context.getSystemService(BluetoothManager::class.java)!!
        val server = manager.openGattServer(context, gattServerCallback)
        val service = BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        server.addService(service) // Advertising starts once this service is successfully added, asynchronously
    }

    fun stopAdvertisingToCentrals() {
        advertiser.stopAdvertising(advertiseCallback)
    }

}