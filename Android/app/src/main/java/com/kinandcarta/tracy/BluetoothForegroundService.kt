package com.kinandcarta.tracy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.kinandcarta.tracy.central.Central
import com.kinandcarta.tracy.peripheral.Peripheral

/**
 * Runs as a foreground service to keep running as a Bluetooth Central and Peripheral
 */
class BluetoothForegroundService : Service() {

    companion object {
        private const val tag = "Tracy - BluetoothForegroundService"
        fun intent(context: Context) = Intent(context, BluetoothForegroundService::class.java)
    }

    private val peripheral = Peripheral()
    private val central = Central()

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        Log.d(tag, "onCreate")
        registerNotificationChannel()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "onStartCommand")
        peripheral.startAdvertisingToCentrals(this) {
            central.startScanningForPeripherals(this)
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        central.stopScanningForPeripherals()
        peripheral.stopAdvertisingToCentrals()
    }

    private fun registerNotificationChannel() {
        val channel = NotificationChannel(
            "BluetoothForegroundService",
            "BluetoothForegroundService",
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description = "Required notification channel"
        channel.setSound(null, null)
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun startForeground() {
        val notification = Notification.Builder(this, "BluetoothForegroundService")
            .setOngoing(true)
            .setProgress(0, 0, true)
            .setContentTitle("Looking for other Tracy users")
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .build()
        startForeground(1, notification)
    }

}