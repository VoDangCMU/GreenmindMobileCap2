package com.vodang.greenmind.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class LocationForegroundService : Service() {
    companion object {
        private const val CHANNEL_ID = "geolocation_channel"
        // Direct flow — no broadcast needed
        val locationFlow = MutableSharedFlow<Location>(replay = 1, extraBufferCapacity = 64)
    }

    private lateinit var fusedClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        startForeground(1, buildNotification())

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val last = result.lastLocation ?: return
                val loc = Location(
                    latitude = last.latitude,
                    longitude = last.longitude,
                    accuracy = last.accuracy,
                    timestampMillis = last.time
                )
                CoroutineScope(Dispatchers.Main).launch {
                    locationFlow.emit(loc)
                }
            }
        }

        requestLocationUpdates()
    }

    private fun requestLocationUpdates() {
        // Use highest-accuracy, frequent updates for real-time tracking
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateDistanceMeters(0f)
            .setMinUpdateIntervalMillis(500L)
            .build()
        try {
            fusedClient.requestLocationUpdates(request, locationCallback, mainLooper)
        } catch (se: SecurityException) {
            // Missing location permission — stop service to avoid crash
            stopSelf()
        } catch (t: Throwable) {
            // Any other error, stop to avoid uncontrolled crashes
            stopSelf()
        }
    }

    private fun buildNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location tracking")
            .setContentText("Collecting location in background")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(CHANNEL_ID, "Location", NotificationManager.IMPORTANCE_LOW)
            mgr.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        try { fusedClient.removeLocationUpdates(locationCallback) } catch (_: Exception) {}
        super.onDestroy()
    }
}
