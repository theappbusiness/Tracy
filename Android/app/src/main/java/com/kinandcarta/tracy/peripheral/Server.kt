package com.kinandcarta.tracy.peripheral

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattServer
import android.bluetooth.BluetoothGattServerCallback
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothManager
import android.content.Context
import android.util.Log
import com.kinandcarta.tracy.characteristicUUID
import com.kinandcarta.tracy.serviceUUID

/**
 * A Server handles incoming requests for data from Centrals.
 * You cannot connect and request data from this device as a Central unless it is discoverable first,
 * which is handled by an Advertiser.
 *
 * @see Advertiser
 */
class Server {

    companion object {
        private const val tag = "Tracy - Server"
    }

    private val gattServerCallback = object : BluetoothGattServerCallback() {
        override fun onServiceAdded(status: Int, service: BluetoothGattService?) {
            Log.d(tag, "Service ${service?.uuid} added with status: $status")
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    Log.d(tag, "Successfully added service")
                    onStarted()
                }
                else -> Log.e(tag, "Could not add service (status code $status), advertising will not start")
            }
        }

        override fun onCharacteristicReadRequest(
            device: BluetoothDevice?,
            requestId: Int,
            offset: Int,
            characteristic: BluetoothGattCharacteristic?
        ) {
            Log.d(
                tag,
                "Read request for ${device?.address}, requestId $requestId, offset: $offset, characteristic: ${characteristic?.uuid}"
            )
            server.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset, android.os.Build.MODEL.toByteArray(Charsets.UTF_8))
        }
    }
    private lateinit var server: BluetoothGattServer
    private lateinit var onStarted: () -> Unit

    /**
     * Starts the Server to be able to handle incoming requests for data.
     * Starting a Server happens asynchronously, which calls the onStarted handler
     * you provide to this function.
     */
    fun start(context: Context, onStarted: () -> Unit) {
        this.onStarted = onStarted
        val manager = context.getSystemService(BluetoothManager::class.java)!!
        server = manager.openGattServer(context, gattServerCallback)
        val service = BluetoothGattService(
            serviceUUID,
            BluetoothGattService.SERVICE_TYPE_PRIMARY
        )
        val characteristic = BluetoothGattCharacteristic(
            characteristicUUID,
            BluetoothGattCharacteristic.PROPERTY_READ,
            BluetoothGattCharacteristic.PERMISSION_READ
        )
        service.addCharacteristic(characteristic)
        server.addService(service)
    }

    /**
     * Stops this server so it cannot process incoming requests.
     */
    fun stop() {
        server.close()
    }

}