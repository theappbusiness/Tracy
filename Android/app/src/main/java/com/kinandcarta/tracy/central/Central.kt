package com.kinandcarta.tracy.central

import android.content.Context
import com.kinandcarta.tracy.peripheral.Peripheral

/**
 * A Central is responsible for scanning / discovering nearby Peripherals,
 * as well as connecting to them to request data from them.
 *
 * @see Peripheral
 */
class Central {

    companion object {
        private const val tag = "Tracy - Central"
    }

    private val scanner = Scanner()
    private val connector = Connector()

    /**
     * Starts scanning for nearby Peripherals.
     * Upon discovery, the Central will attempt to connect and read the
     * custom data to identify that device
     * (which is just the name of the device for the purposes of this demo!)
     */
    fun startScanningForPeripherals(context: Context) {
        scanner.startScanning { connector.connect(it, context) }
    }

    /**
     * Stops scanning for nearby Peripherals
     * and disconnects any currently discovered Peripherals.
     */
    fun stopScanningForPeripherals() {
        scanner.stopScanning()
        connector.disconnectAll()
    }

}
