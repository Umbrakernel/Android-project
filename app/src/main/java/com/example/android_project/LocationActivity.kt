package com.example.android_project

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.android_project.BackgroundService

class LocationActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }

    lateinit var bBackToMain: Button
    lateinit var bStartTelemetry: Button
    lateinit var bStopTelemetry: Button

    lateinit var tvLat: TextView
    lateinit var tvLon: TextView
    lateinit var tvAlt: TextView
    lateinit var tvTime: TextView
    lateinit var tvAccuracy: TextView
    lateinit var tvRsrp: TextView
    lateinit var tvRsrq: TextView
    lateinit var tvRssi: TextView
    lateinit var tvRssnr: TextView
    lateinit var tvNetworkType: TextView
    lateinit var tvOperator: TextView

    val bgReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val rsrp = intent?.getIntExtra("RSRP", Int.MIN_VALUE) ?: Int.MIN_VALUE
            val rsrq = intent?.getIntExtra("RSRQ", Int.MIN_VALUE) ?: Int.MIN_VALUE
            val rssi = intent?.getIntExtra("RSSI", Int.MIN_VALUE) ?: Int.MIN_VALUE
            val rssnr = intent?.getFloatExtra("RSSNR", Float.NaN) ?: Float.NaN
            val accuracy = intent?.getFloatExtra("Accuracy", -1f) ?: -1f
            val latitude = intent?.getDoubleExtra("Latitude", Double.NaN) ?: Double.NaN
            val longitude = intent?.getDoubleExtra("Longitude", Double.NaN) ?: Double.NaN
            val altitude = intent?.getDoubleExtra("Altitude", Double.NaN) ?: Double.NaN
            val timeMs = intent?.getLongExtra("TimeMs", -1L) ?: -1L
            val networkType = intent?.getStringExtra("NetworkType") ?: "UNKNOWN"
            val operatorName = intent?.getStringExtra("OperatorName") ?: "UNKNOWN"

            tvLat.text = if (latitude.isNaN()) "Latitude: N/A" else "Latitude: $latitude"
            tvLon.text = if (longitude.isNaN()) "Longitude: N/A" else "Longitude: $longitude"
            tvAlt.text = if (altitude.isNaN()) "Altitude: N/A" else "Altitude: $altitude"
            tvTime.text = if (timeMs < 0L) "Time: N/A" else "Time: $timeMs"
            tvRsrp.text = if (rsrp == Int.MIN_VALUE) "RSRP: N/A" else "RSRP: $rsrp dBm"
            tvRsrq.text = if (rsrq == Int.MIN_VALUE) "RSRQ: N/A" else "RSRQ: $rsrq dBm"
            tvRssi.text = if (rssi == Int.MIN_VALUE) "RSSI: N/A" else "RSSI: $rssi dBm"
            tvRssnr.text = if (rssnr.isNaN()) "RSSNR: N/A" else "RSSNR: $rssnr dBm"
            tvAccuracy.text = if (accuracy < 0f) "Accuracy: N/A" else "Accuracy: $accuracy m"
            tvNetworkType.text = "Network type: $networkType"
            tvOperator.text = "Operator: $operatorName"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_location)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        bBackToMain = findViewById(R.id.back_to_main)
        bStartTelemetry = findViewById(R.id.btn_start_telemetry)
        bStopTelemetry = findViewById(R.id.btn_stop_telemetry)

        tvLat = findViewById(R.id.tv_lat)
        tvLon = findViewById(R.id.tv_lon)
        tvAlt = findViewById(R.id.tv_alt)
        tvTime = findViewById(R.id.tv_time)
        tvAccuracy = findViewById(R.id.tv_accuracy)
        tvRsrp = findViewById(R.id.tv_rsrp)
        tvRsrq = findViewById(R.id.tv_rsrq)
        tvRssi = findViewById(R.id.tv_rssi)
        tvRssnr = findViewById(R.id.tv_rssnr)
        tvNetworkType = findViewById(R.id.tv_network_type)
        tvOperator = findViewById(R.id.tv_operator)

        tvLat.text = "Latitude: -"
        tvLon.text = "Longitude: -"
        tvAlt.text = "Altitude: -"

        bBackToMain.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

        bStartTelemetry.setOnClickListener {
            if (hasRequiredPermissions()) {
                startBackgroundService()
                Toast.makeText(this, "BackgroundService started", Toast.LENGTH_SHORT).show()
            } else {
                requestPermissionsFromUser()
            }
        }

        bStopTelemetry.setOnClickListener {
            stopService(Intent(this, BackgroundService::class.java))
            Toast.makeText(this, "BackgroundService stopped", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(bgReceiver, IntentFilter("BackGroundUpdate"))
    }

    override fun onStop() {
        super.onStop()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(bgReceiver)
    }

    fun startBackgroundService() {
        val serviceIntent = Intent(this, BackgroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    fun hasRequiredPermissions(): Boolean {
        val locationGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

        val phoneGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED

        val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return locationGranted && phoneGranted && notificationGranted
    }

    fun requestPermissionsFromUser() {
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.READ_PHONE_STATE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions += Manifest.permission.POST_NOTIFICATIONS
        }

        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            PERMISSION_REQUEST_CODE
        )
    }
}
