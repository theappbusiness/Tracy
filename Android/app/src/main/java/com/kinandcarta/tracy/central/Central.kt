package com.kinandcarta.tracy.central

import android.content.Context

class Central {

    companion object {
        private const val tag = "Tracy - Central"
    }

    private lateinit var context: Context
    private val scanner = Scanner { connector.connect(it, context) }
    private val connector = Connector()

    fun startScanningForPeripherals(context: Context) {
        this.context = context
        scanner.startScanning()
    }

    fun stopScanningForPeripherals() {
        scanner.stopScanning()
        connector.disconnectAll()
    }

}
