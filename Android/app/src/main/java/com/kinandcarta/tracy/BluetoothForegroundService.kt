package com.kinandcarta.tracy

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder

class BluetoothForegroundService : Service() {

    companion object {
        fun intent(context: Context) = Intent(context, BluetoothForegroundService::class.java)
    }

    private val central = Central()

    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        registerNotificationChannel()
        startForeground()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        central.startScanningForPeripherals()
        return START_STICKY
    }

    override fun onDestroy() {
        central.stopScanningForPeripherals()
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
            .setContentTitle("Tracy is looking for other devices with Tracy installed")
            .build()
        startForeground(1, notification)
    }

}