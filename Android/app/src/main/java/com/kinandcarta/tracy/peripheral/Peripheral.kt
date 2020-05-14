package com.kinandcarta.tracy.peripheral

import android.content.Context

class Peripheral {

    companion object {
        private const val tag = "Tracy - Peripheral"
    }

    private val advertiser = Advertiser()
    private val server = Server()

    fun startAdvertisingToCentrals(context: Context) {
        server.start(context) {
            advertiser.startAdvertising()
        }
    }

    fun stopAdvertisingToCentrals() {
        advertiser.stopAdvertising()
        server.stop()
    }

}
