package com.example.android_project

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.TelephonyManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import org.zeromq.SocketType
import org.zeromq.ZContext
import org.zeromq.ZMQ
import java.io.File

class BackgroundService : Service(), LocationListener {

    val LOG_TAG: String = "BG_SERVICE"
    val serviceJob = Job()
    val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    lateinit var locationManager: LocationManager
    lateinit var telephonyManager: TelephonyManager
    var lastLocation: Location? = null
    var loopStarted = false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun sendMessageToActivity(
        msg: String?,
        rsrp: Int?,
        rsrq: Int?,
        rssi: Int?,
        rssnr: Float?,
        accuracy: Float?,
        latitude: Double?,
        longitude: Double?,
        altitude: Double?,
        timeMs: Long?,
        networkType: String,
        operatorName: String
    ) {
        val intent = Intent("BackGroundUpdate")
        intent.putExtra("Status", msg)
        intent.putExtra("RSRP", rsrp ?: Int.MIN_VALUE)
        intent.putExtra("RSRQ", rsrq ?: Int.MIN_VALUE)
        intent.putExtra("RSSI", rssi ?: Int.MIN_VALUE)
        intent.putExtra("RSSNR", rssnr ?: Float.NaN)
        intent.putExtra("Accuracy", accuracy ?: -1f)
        intent.putExtra("Latitude", latitude ?: Double.NaN)
        intent.putExtra("Longitude", longitude ?: Double.NaN)
        intent.putExtra("Altitude", altitude ?: Double.NaN)
        intent.putExtra("TimeMs", timeMs ?: -1L)
        intent.putExtra("NetworkType", networkType)
        intent.putExtra("OperatorName", operatorName)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildNotification("Telemetry collection in background"))
        startLocationUpdates()

        if (!loopStarted) {
            loopStarted = true
            serviceScope.launch {
                for (i in 0 until 1000000) {
                    delay(1000)

                    val location = lastLocation
                    val network = collectNetworkInfo()

                    if (location != null) {
                        val payload = JSONObject().apply {
                            put("time", System.currentTimeMillis())
                            put("latitude", location.latitude)
                            put("longitude", location.longitude)
                            put("altitude", location.altitude)
                            put("accuracy", location.accuracy.toDouble())
                            put("rsrp", network.rsrp ?: JSONObject.NULL)
                            put("rsrq", network.rsrq ?: JSONObject.NULL)
                            put("rssi", network.rssi ?: JSONObject.NULL)
                            put("rssnr", network.rssnr ?: JSONObject.NULL)
                            put("networkType", network.networkType)
                            put("operatorName", network.operatorName)
                            put("CellInfoLte", network.cellInfoLte ?: JSONObject.NULL)
                            put("CellInfoGsm", network.cellInfoGsm ?: JSONObject.NULL)
                            put("CellInfoNr", network.cellInfoNr ?: JSONObject.NULL)
                        }
                        savePayloadToPhone(payload)
                        sendWithSockets(payload.toString())
                    }

                    val status = "Task running: $i"
                    sendMessageToActivity(
                        msg = status,
                        rsrp = network.rsrp,
                        rsrq = network.rsrq,
                        rssi = network.rssi,
                        rssnr = network.rssnr,
                        accuracy = location?.accuracy,
                        latitude = location?.latitude,
                        longitude = location?.longitude,
                        altitude = location?.altitude,
                        timeMs = location?.time,
                        networkType = network.networkType,
                        operatorName = network.operatorName
                    )
                    Log.d(
                        LOG_TAG,
                        "$status, rsrp=${network.rsrp}, rsrq=${network.rsrq}, rssi=${network.rssi}, rssnr=${network.rssnr}"
                    )
                }
                stopSelf()
            }
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        loopStarted = false
        serviceJob.cancel()
        if (hasLocationPermission()) {
            locationManager.removeUpdates(this)
        }
        Log.d(LOG_TAG, "Service destroyed")
    }

    override fun onLocationChanged(location: Location) {
        lastLocation = location
    }

    fun hasLocationPermission(): Boolean {
        val fine = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fine || coarse
    }

    fun hasPhonePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun startLocationUpdates() {
        if (!hasLocationPermission()) return

        val gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (gpsEnabled) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, this)
            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { lastLocation = it }
        }

        if (networkEnabled) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 0f, this)
            locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let { lastLocation = it }
        }
    }

    data class NetworkInfo(
        val rsrp: Int?,
        val rsrq: Int?,
        val rssi: Int?,
        val rssnr: Float?,
        val networkType: String,
        val operatorName: String,
        val cellInfoLte: JSONObject?,
        val cellInfoGsm: JSONObject?,
        val cellInfoNr: JSONObject?
    )

    fun collectNetworkInfo(): NetworkInfo {
        if (!hasPhonePermission()) {
            return NetworkInfo(null, null, null, null, "UNKNOWN", "UNKNOWN", null, null, null)
        }

        val networkType = when (telephonyManager.dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_NR -> "5G"
            TelephonyManager.NETWORK_TYPE_LTE -> "4G LTE"
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_UMTS -> "3G"
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_GSM -> "2G"
            else -> "UNKNOWN"
        }

        val operatorName = telephonyManager.networkOperatorName ?: "UNKNOWN"

        var rsrp: Int? = null
        var rsrq: Int? = null
        var rssi: Int? = null
        var rssnr: Float? = null
        var cellInfoLte: JSONObject? = null
        var cellInfoGsm: JSONObject? = null
        var cellInfoNr: JSONObject? = null

        if (hasLocationPermission()) {
            val registered = telephonyManager.allCellInfo?.firstOrNull(CellInfo::isRegistered)
            when (registered) {
                is CellInfoLte -> {
                    val identity = registered.cellIdentity
                    val signal = registered.cellSignalStrength
                    rsrp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) signal.rsrp else signal.dbm
                    rsrq = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) signal.rsrq else null
                    rssi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) signal.rssi else signal.dbm
                    rssnr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) signal.rssnr / 10.0f else null
                    cellInfoLte = JSONObject().apply {
                        put(
                            "CellIdentityLte",
                            JSONObject().apply {
                                put("Band", JSONObject.NULL)
                                put("CellIdentity", identity.ci)
                                put("EARFCN", identity.earfcn)
                                put("MCC", identity.mccString ?: JSONObject.NULL)
                                put("MNC", identity.mncString ?: JSONObject.NULL)
                                put("PCI", identity.pci)
                                put("TAC", identity.tac)
                            }
                        )
                        put(
                            "CellSignalStrengthLte",
                            JSONObject().apply {
                                put("ASU Level", signal.asuLevel)
                                put("CQI", signal.cqi)
                                put("RSRP", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) signal.rsrp else JSONObject.NULL)
                                put("RSRQ", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) signal.rsrq else JSONObject.NULL)
                                put("RSSI", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) signal.rssi else JSONObject.NULL)
                                put("RSSNR", if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) signal.rssnr / 10.0 else JSONObject.NULL)
                                put("Timing Advance", signal.timingAdvance)
                            }
                        )
                    }
                }

                is CellInfoGsm -> {
                    val identity = registered.cellIdentity
                    val signal = registered.cellSignalStrength
                    rssi = signal.dbm
                    cellInfoGsm = JSONObject().apply {
                        put(
                            "CellIdentityGSM",
                            JSONObject().apply {
                                put("CellIdentity", identity.cid)
                                put("BSIC", identity.bsic)
                                put("ARFCN", identity.arfcn)
                                put("LAC", identity.lac)
                                put("MCC", identity.mccString ?: JSONObject.NULL)
                                put("MNC", identity.mncString ?: JSONObject.NULL)
                                put("PSC", JSONObject.NULL)
                            }
                        )
                        put(
                            "CellSignalStrengthGsm",
                            JSONObject().apply {
                                put("Dbm", signal.dbm)
                                put("RSSI", signal.rssi)
                                put("Timing Advance", signal.timingAdvance)
                            }
                        )
                    }
                }

                is CellInfoNr -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val identity = registered.cellIdentity
                        val signal = registered.cellSignalStrength
                        rsrp = signal.ssRsrp
                        rsrq = signal.ssRsrq
                        rssnr = signal.ssSinr.toFloat()
                        cellInfoNr = JSONObject().apply {
                            put(
                                "CellIdentityNr",
                                JSONObject().apply {
                                    put("Band", JSONObject.NULL)
                                    put("NCI", identity.nci)
                                    put("PCI", identity.pci)
                                    put("Nrargcn", identity.nrarfcn)
                                    put("TAC", identity.tac)
                                    put("MCC", identity.mccString ?: JSONObject.NULL)
                                    put("MNC", identity.mncString ?: JSONObject.NULL)
                                }
                            )
                            put(
                                "CellSignalStrengthNr",
                                JSONObject().apply {
                                    put("SS-RSRP", signal.ssRsrp)
                                    put("SS-RSRQ", signal.ssRsrq)
                                    put("SS-SINR", signal.ssSinr)
                                    put("Timing Advance", signal.timingAdvance)
                                }
                            )
                        }
                    }
                }
            }
        }

        return NetworkInfo(
            rsrp = rsrp,
            rsrq = rsrq,
            rssi = rssi,
            rssnr = rssnr,
            networkType = networkType,
            operatorName = operatorName,
            cellInfoLte = cellInfoLte,
            cellInfoGsm = cellInfoGsm,
            cellInfoNr = cellInfoNr
        )
    }

    fun sendWithSockets(message: String) {
        val context = ZContext()
        val socket = context.createSocket(SocketType.REQ)
        try {
            socket.connect(BACKEND_ENDPOINT)
            socket.receiveTimeOut = 3000
            socket.send(message.toByteArray(ZMQ.CHARSET), 0)
            socket.recv(0)
        } catch (_: Throwable) {
        } finally {
            socket.close()
            context.close()
        }
    }

    fun savePayloadToPhone(payload: JSONObject) {
        try {
            val file = File(filesDir, "telemetry_data.jsonl")
            file.appendText(payload.toString() + "\n")
        } catch (_: Throwable) {
        }
    }

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(com.example.android_project.R.mipmap.ic_launcher)
            .setContentTitle("Background service")
            .setContentText(text)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val CHANNEL_ID = "bg_service_channel"
        const val NOTIFICATION_ID = 1201
        const val BACKEND_ENDPOINT = "tcp://10.163.3.63:5555"
    }
}

