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

class Connector {

    companion object {
        private const val tag = "Tracy - Connector"
    }

    private lateinit var context: Context
    private val pendingConnections =
        mutableSetOf<BluetoothDevice>() // You can only perform one Bluetooth operation at once, including connecting, so you have to queue them!
    private val connections = mutableSetOf<BluetoothGatt>()
    private val connectionCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            Log.d(tag, "Connection state changed for ${gatt?.device?.address} to $newState with status code $status")
            if (gatt == null) {
                Log.e(tag, "Gatt is null, cannot continue!")
                return
            }
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> onConnectionStateChangeSuccess(gatt, newState)
                else -> onConnectionStateChangeFailure(gatt, status)
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            Log.d(tag, "onServicesDiscovered - ${gatt?.device?.address} with status code $status")
            if (gatt == null) {
                Log.e(tag, "Gatt is null, cannot continue!")
                return
            }
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> onServicesDiscoveredSuccess(gatt)
                else -> onServicesDiscoveredFailure(gatt, status)
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
            Log.d(tag, "onCharacteristicRead - ${gatt?.device?.address} ${characteristic?.uuid} with status code $status")
            if (gatt == null) {
                Log.e(tag, "Gatt is null, cannot continue!")
                return
            }
            if (characteristic == null) {
                Log.e(tag, "Characteristic is null, cannot continue!")
                removePendingAndConnectNextIfAny(gatt.device)
                return
            }
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> onCharacteristicReadSuccess(gatt, characteristic)
                else -> onCharacteristicReadFailure(gatt, characteristic, status)
            }
        }
    }

    fun connect(device: BluetoothDevice, context: Context) {
        this.context = context
        pendingConnections.add(device)
        if (pendingConnections.size > 1) {
            Log.d(tag, "Still awaiting previous connection(s) to complete")
            return
        }
        connectNextPendingIfAny()
    }

    fun disconnectAll() {
        pendingConnections.clear()
        connections.clear()
    }

    private fun connectNextPendingIfAny() {
        val next = pendingConnections.firstOrNull() ?: return
        Log.d(tag, "Connecting next device ${next.address}")
        next.connectGatt(context, false, connectionCallback)
    }

    private fun removePendingAndConnectNextIfAny(bluetoothDevice: BluetoothDevice) {
        if (!pendingConnections.remove(bluetoothDevice)) {
            Log.d(tag, "Device was not pending so cannot remove from pending connections ${bluetoothDevice.address}")
            return
        }
        connectNextPendingIfAny()
    }

    private fun onConnectionStateChangeSuccess(gatt: BluetoothGatt, newState: Int) {
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                Log.d(tag, "Connected to ${gatt.device.address}")
                connections.add(gatt)
                gatt.discoverServices()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.d(tag, "Disconnected from ${gatt.device.address}")
                connections.remove(gatt)
                gatt.close()
                removePendingAndConnectNextIfAny(gatt.device)
            }
        }
    }

    private fun onConnectionStateChangeFailure(gatt: BluetoothGatt, status: Int) {
        Log.e(tag, "Connection state error for ${gatt.device.address} with status code $status")
        connections.remove(gatt)
        removePendingAndConnectNextIfAny(gatt.device)
    }

    private fun onServicesDiscoveredSuccess(gatt: BluetoothGatt) {
        Log.d(tag, "Discovered services successfully for ${gatt.device.address}")
        val service = gatt.services.firstOrNull { it.uuid == serviceUUID }
        if (service == null) {
            Log.e(tag, "Expected service does not exist ${gatt.device.address}")
            removePendingAndConnectNextIfAny(gatt.device)
            return
        }
        val characteristic = service.characteristics.firstOrNull { it.uuid == characteristicUUID }
        if (characteristic == null) {
            Log.e(tag, "Expected characteristic does not exist ${gatt.device.address}")
            removePendingAndConnectNextIfAny(gatt.device)
            return
        }
        gatt.readCharacteristic(characteristic)
    }

    private fun onServicesDiscoveredFailure(gatt: BluetoothGatt, status: Int) {
        Log.e(tag, "Failed to discover services for ${gatt.device.address} with status code $status")
        removePendingAndConnectNextIfAny(gatt.device)
    }

    private fun onCharacteristicReadSuccess(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val value = characteristic.value
        val stringValue = String(value, Charsets.UTF_8)
        Log.d(tag, "Read characteristic ${characteristic.uuid} for ${gatt.device.address} with value $stringValue")
        gatt.disconnect()
    }

    private fun onCharacteristicReadFailure(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic, status: Int) {
        Log.e(tag, "Failed to read characteristic ${characteristic.uuid} for ${gatt.device.address} with status code $status")
        removePendingAndConnectNextIfAny(gatt.device)
    }

}