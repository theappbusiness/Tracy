package com.kinandcarta.tracy.central

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.util.Log

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
            Log.v(
                tag,
                "Connection state changed for ${gatt?.device?.address} to $newState with status code $status"
            )
            if (gatt == null) {
                Log.e(tag, "Gatt is null, cannot continue!")
                return
            }
            val wasPending = pendingConnections.remove(gatt.device)
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> onConnectionStateChangeSuccess(gatt, newState)
                else -> onConnectionStateChangeFailure(gatt, status, wasPending)
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
        next.connectGatt(context, false, connectionCallback)
    }

    private fun onConnectionStateChangeSuccess(gatt: BluetoothGatt, newState: Int) {
        when (newState) {
            BluetoothProfile.STATE_CONNECTED -> {
                Log.d(tag, "Connected to ${gatt.device.address}")
                connections.add(gatt)
                connectNextPendingIfAny()
            }
            BluetoothProfile.STATE_DISCONNECTED -> {
                Log.d(tag, "Disconnected from ${gatt.device.address}")
                connections.remove(gatt)
            }
        }
    }

    private fun onConnectionStateChangeFailure(
        gatt: BluetoothGatt,
        status: Int,
        wasPending: Boolean
    ) {
        Log.e(tag, "Connection state error for ${gatt.device.address} with status code $status")
        connections.remove(gatt)
        if (wasPending) connectNextPendingIfAny()
    }

}