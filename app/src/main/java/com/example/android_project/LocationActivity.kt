package com.example.android_project

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.android_project.MainActivity
import com.example.android_project.R
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class LocationActivity : LocationListener, AppCompatActivity()  {

    val LOG_TAG: String = "LOCATION_ACTIVITY"

    companion object {
        const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    lateinit var locationManager: LocationManager
    lateinit var bBackToMain: Button

    lateinit var tvLat: TextView
    lateinit var tvLon: TextView
    lateinit var tvAlt: TextView
    lateinit var tvTime: TextView

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

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        tvLat = findViewById(R.id.tv_lat)
        tvLon = findViewById(R.id.tv_lon)
        tvAlt = findViewById(R.id.tv_alt)
        tvTime = findViewById(R.id.tv_time)
    }

    override fun onResume() {
        super.onResume()

        bBackToMain.setOnClickListener {
            val backToMain = Intent(this, MainActivity::class.java)
            startActivity(backToMain)
        }

        updateCurrentLocation()
    }

    fun updateCurrentLocation() {

        if(checkPermissions()) {
            if(isLocationEnabled()) {

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissions()
                    return
                }

                val lastLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (lastLoc != null) {
                    onLocationChanged(lastLoc)
                }

                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    1f,
                    this
                )

            } else {
                Toast.makeText(applicationContext, "Включите геолокацию в настройках", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            Log.w(LOG_TAG, "разрешение на определение местоположения не дано")
            tvLat.setText("Разрешение не дано")
            tvLon.setText("Разрешение не дано")
            tvAlt.setText("Разрешение не дано")
            tvTime.setText("Разрешение не дано")
            requestPermissions()
        }
    }

    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Разрешение дано", Toast.LENGTH_SHORT).show()
                updateCurrentLocation()
            } else {
                Toast.makeText(applicationContext, "Отклонено", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    override fun onLocationChanged(location: Location) {

        tvLat.setText(location.latitude.toString())
        tvLon.setText(location.longitude.toString())
        tvAlt.setText(location.altitude.toString())
        tvTime.setText(location.time.toString())

        saveJson(location)
    }

    fun saveJson(location: Location) {

        val json = JSONObject(
            mapOf(
                "latitude" to location.latitude,
                "longitude" to location.longitude,
                "altitude" to location.altitude,
                "time" to location.time
            )
        )

        val file = File(filesDir, "locations.json")
        file.appendText(json.toString() + "\n")

        Log.d(LOG_TAG, "Saved: $json")
    }
}
