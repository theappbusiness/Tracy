package com.kinandcarta.tracy.peripheral

import android.content.Context
import com.kinandcarta.tracy.central.Central

/**
 * A Peripheral is responsible for advertising this device to be discovered by other Centrals,
 * as well as responsible for handling requests for data from other Centrals as the server.
 *
 * @see Central
 */
class Peripheral {

    private val advertiser = Advertiser()
    private val server = Server()

    /**
     * Starts advertising this device so other Centrals can discover it
     * and request data.
     *
     * The process to fully start the Peripheral is asynchronous, and once
     * complete the onStart handler will be called.
     */
    fun startAdvertisingToCentrals(context: Context, onStart: () -> Unit) {
        server.start(context) {
            advertiser.startAdvertising {
                onStart()
            }
        }
    }

    /**
     * Stops advertising so other Centrals cannot discover this device
     * or request data.
     */
    fun stopAdvertisingToCentrals() {
        advertiser.stopAdvertising()
        server.stop()
    }

}
