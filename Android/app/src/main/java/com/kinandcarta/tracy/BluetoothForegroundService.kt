package com.kinandcarta.tracy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log

class BluetoothForegroundService : Service() {

    companion object {
        private const val tag = "Tracy - BluetoothForegroundService"
        fun intent(context: Context) = Intent(context, BluetoothForegroundService::class.java)
    }

    private val central = Central()
    private val peripheral = Peripheral()

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        Log.v(tag, "onCreate")
        registerNotificationChannel()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.v(tag, "onStartCommand")
        central.startScanningForPeripherals()
        peripheral.startAdvertisingToCentrals()
        return START_STICKY
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
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun startForeground() {
        val notification = Notification.Builder(this, "BluetoothForegroundService")
            .setOngoing(true)
            .setProgress(0, 0, true)
            .setContentTitle("Looking for other Tracy users")
            .setContentText("Tracy is looking for other devices with Tracy installed")
            .build()
        startForeground(1, notification)
    }

}