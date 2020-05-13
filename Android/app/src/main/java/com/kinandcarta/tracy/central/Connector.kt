package com.kinandcarta.tracy.central

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log
import com.kinandcarta.tracy.characteristicUUID
import com.kinandcarta.tracy.serviceUUID
import java.lang.IllegalStateException

class Connector {

    companion object {
        private const val tag = "Tracy - Connector"
    }

    private lateinit var context: Context
    private val pendingConnections = mutableSetOf<BluetoothDevice>() // You can only perform one Bluetooth operation at once, including connecting, so you have to queue them!
    private val connections = mutableSetOf<BluetoothGatt>()
    private val connectionCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d(tag, "Connection state changed for ${gatt?.device?.address} to $newState with status code $status")
            if (gatt == null) throw IllegalStateException("Gatt is null, cannot continue!")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> onConnectionStateChangeSuccess(gatt, newState)
                else -> onConnectionStateChangeFailure(gatt, status)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d(tag, "onServicesDiscovered - ${gatt?.device?.address} with status code $status")
            if (gatt == null) throw IllegalStateException("Gatt is null, cannot continue!")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> onServicesDiscoveredSuccess(gatt)
                else -> onServicesDiscoveredFailure(gatt, status)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.d(tag, "onCharacteristicRead - ${gatt?.device?.address} ${characteristic?.uuid} with status code $status")
            if (gatt == null) throw IllegalStateException("Gatt is null, cannot continue!")
            if (characteristic == null) throw IllegalStateException("Characteristic is null, cannot continue!")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> onCharacteristicReadSuccess(gatt, characteristic)
                else -> onCharacteristicReadFailure(gatt, characteristic, status)
            }
        }
    }

    /**
     * Connects the device, or queues it for connection if another connection is in progress.
     */
    fun connect(device: BluetoothDevice, context: Context) {
        this.context = context
        pendingConnections.add(device)
        if (pendingConnections.size > 1) {
            Log.d(tag, "Still awaiting ${pendingConnections.size} previous connection(s) to complete")
            return
        }
        connectNextPendingIfAny()
    }

    /**
     * Disconnects all connected and removes any pending connections.
     */
    fun disconnectAll() {
        pendingConnections.clear()
        connections.forEach { it.close() }
        connections.clear()
    }

    private fun connectNextPendingIfAny() {
        val nextDeviceToConnect = pendingConnections.firstOrNull() ?: return
        Log.d(tag, "Connecting next device ${nextDeviceToConnect.address}")
        val gatt = nextDeviceToConnect.connectGatt(context, false, connectionCallback, BluetoothDevice.TRANSPORT_LE)
        connections.add(gatt)
    }

    private fun removePendingAndConnectNextIfAny(bluetoothGatt: BluetoothGatt) {
        with(bluetoothGatt) {
            disconnect()
            close()
            connections.remove(this)
            if (!pendingConnections.remove(device)) Log.d(tag, "Device was not pending so cannot remove from pending connections ${bluetoothGatt.device.address}")
            else connectNextPendingIfAny()
        }
    }

    private fun onConnectionStateChangeSuccess(gatt: BluetoothGatt, newState: Int) {
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                Log.d(tag, "Connected to ${gatt.device.address}")
                gatt.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.d(tag, "Disconnected from ${gatt.device.address}")
                removePendingAndConnectNextIfAny(gatt)
            }
        }
    }

    private fun onConnectionStateChangeFailure(gatt: BluetoothGatt, status: Int) {
        Log.e(tag, "Connection state error for ${gatt.device.address} with status code $status")
        removePendingAndConnectNextIfAny(gatt)
    }

    private fun onServicesDiscoveredSuccess(gatt: BluetoothGatt) {
        Log.d(tag, "Discovered services successfully for ${gatt.device.address}")
        val service = gatt.services.firstOrNull { it.uuid == serviceUUID }
        if (service == null) {
            Log.e(tag, "Expected service does not exist ${gatt.device.address}")
            throw IllegalStateException()
        }
        val characteristic = service.characteristics.firstOrNull { it.uuid == characteristicUUID }
        if (characteristic == null) {
            Log.e(tag, "Expected characteristic does not exist ${gatt.device.address}")
            throw IllegalStateException()
        }
        gatt.readCharacteristic(characteristic)
    }

    private fun onServicesDiscoveredFailure(gatt: BluetoothGatt, status: Int) {
        Log.e(tag, "Failed to discover services for ${gatt.device.address} with status code $status")
        removePendingAndConnectNextIfAny(gatt)
    }

    private fun onCharacteristicReadSuccess(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val value = characteristic.value
        val stringValue = String(value, Charsets.UTF_8)
        Log.d(tag, "Read characteristic ${characteristic.uuid} for ${gatt.device.address} with value $stringValue")
        gatt.disconnect()
    }

    private fun onCharacteristicReadFailure(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        Log.e(tag, "Failed to read characteristic ${characteristic.uuid} for ${gatt.device.address} with status code $status")
        removePendingAndConnectNextIfAny(gatt)
    }

}