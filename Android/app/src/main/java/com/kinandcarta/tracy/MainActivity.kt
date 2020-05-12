package com.kinandcarta.tracy

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.lang.IllegalStateException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        permissions.withIndex().forEach {
            when (it.value) {
                Manifest.permission.ACCESS_FINE_LOCATION -> {
                    when (grantResults[it.index]) {
                        PackageManager.PERMISSION_GRANTED -> startService(BluetoothForegroundService.intent(this))
                        else -> throw IllegalStateException()
                    }
                }
                else -> throw IllegalStateException()
            }
        }
    }
}
