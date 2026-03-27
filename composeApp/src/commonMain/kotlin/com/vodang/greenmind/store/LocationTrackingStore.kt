package com.vodang.greenmind.store

import com.vodang.greenmind.api.location.CreateLocationRequest
import com.vodang.greenmind.api.location.createLocation
import com.vodang.greenmind.api.location.getLatestLocation
import com.vodang.greenmind.api.nominatim.ReverseOptions
import com.vodang.greenmind.api.nominatim.nominatimReverse
import com.vodang.greenmind.location.Geo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

// Called every 55 s from LocationForegroundService (Android foreground service —
// survives app closure). All logic lives here in commonMain.

const val LOCATION_TICK_MS            = 55_000L  // default; runtime value from SettingsStore
private const val NOMINATIM_THRESHOLD = 100.0   // only re-geocode if moved > 100 m
private const val MAX_RECENT          = 5

data class GpsTick(
    val timestampMs: Long,
    val latitude: Double,
    val longitude: Double,
    val distanceMeters: Double,   // raw metres to previous point (0.0 = stationary)
    val address: String,
)

object LocationTrackingStore {

    private val _recentTicks = MutableStateFlow<List<GpsTick>>(emptyList())
    val recentTicks: StateFlow<List<GpsTick>> = _recentTicks.asStateFlow()

    // ── Nominatim address cache ───────────────────────────────────────────────
    private var cachedAddress: String = ""
    private var cacheAtLat: Double = Double.NaN
    private var cacheAtLon: Double = Double.NaN

    // ── Core tick — called by the foreground service loop ─────────────────────

    suspend fun tick() {
        if (!SettingsStore.locationEnabled.value) return
        val token  = SettingsStore.getAccessToken() ?: return
        val userId = SettingsStore.getUser()?.id    ?: return

        // Wait up to 4 s for a GPS fix (SharedFlow replay=1 returns instantly when cached)
        val current = withTimeoutOrNull(4_000L) {
            Geo.service.locationUpdates.first()
        } ?: return  // no GPS fix yet — skip this tick

        // Fetch the last server-saved point to measure displacement.
        // If none exists (first ever save, or API error) → distance = 0, which is correct:
        // there is no "previous location" to measure from.
        val lastSaved = try {
            getLatestLocation(token).data
        } catch (_: Throwable) {
            null  // 404 / network error — treat as "no previous point"
        }

        val distanceMeters = if (lastSaved != null) {
            haversineMeters(
                current.latitude, current.longitude,
                lastSaved.latitude, lastSaved.longitude
            )
        } else {
            0.0   // no previous point → distance is undefined, store 0
        }

        // Reject vehicle speeds — read thresholds live from SettingsStore
        val minMove    = SettingsStore.minMoveMeters.value.toDouble()
        val maxSpeed   = SettingsStore.maxWalkSpeedMs.value.toDouble()
        val elapsedSec = SettingsStore.locationIntervalMs.value / 1000.0
        if (distanceMeters / elapsedSec > maxSpeed) return

        val lengthToPrevious = if (distanceMeters < minMove) 0.0001
                               else distanceMeters

        // Reverse-geocode only when moved far enough (Nominatim rate-limit guard)
        val address = reverseCached(current.latitude, current.longitude)

        try {
            createLocation(
                token, CreateLocationRequest(
                    userId = userId,
                    address = address,
                    latitude = current.latitude,
                    longitude = current.longitude,
                    lengthToPreviousLocation = lengthToPrevious
                )
            )
            val tick = GpsTick(
                timestampMs    = current.timestampMillis,
                latitude       = current.latitude,
                longitude      = current.longitude,
                distanceMeters = distanceMeters,
                address        = address,
            )
            _recentTicks.value = (_recentTicks.value + tick).takeLast(MAX_RECENT)
        } catch (_: Throwable) {
            // Silent fail — foreground service will retry on the next tick
        }
    }

    // ── Nominatim with cache ──────────────────────────────────────────────────

    private suspend fun reverseCached(lat: Double, lon: Double): String {
        if (cachedAddress.isNotBlank() && !cacheAtLat.isNaN()) {
            if (haversineMeters(lat, lon, cacheAtLat, cacheAtLon) < NOMINATIM_THRESHOLD) {
                return cachedAddress
            }
        }
        return try {
            val place = nominatimReverse(lat, lon, ReverseOptions(zoom = 18, addressDetails = false))
            cachedAddress = place.display_name
            cacheAtLat = lat
            cacheAtLon = lon
            cachedAddress
        } catch (_: Throwable) {
            cachedAddress.ifBlank { "$lat,$lon" }
        }
    }
}

// ── Haversine — distance in metres ───────────────────────────────────────────

fun haversineMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val R = 6_371_000.0
    val dLat = (lat2 - lat1).toRad()
    val dLon = (lon2 - lon1).toRad()
    val a = sin(dLat / 2).pow(2) +
            cos(lat1.toRad()) * cos(lat2.toRad()) * sin(dLon / 2).pow(2)
    return R * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private fun Double.toRad() = this * PI / 180.0
