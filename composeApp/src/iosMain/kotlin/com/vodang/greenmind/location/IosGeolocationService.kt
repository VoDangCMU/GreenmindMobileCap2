package com.vodang.greenmind.location

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import platform.CoreLocation.*
import platform.Foundation.NSObject

actual class GeolocationService actual constructor() {
    private val _updates = MutableSharedFlow<Location>(replay = 1)
    actual val locationUpdates: Flow<Location> = _updates.asSharedFlow()

    private val locationManager = CLLocationManager()

    private val delegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val locs = didUpdateLocations.filterIsInstance<CLLocation>()
            val last = locs.lastOrNull() ?: return
            val lat = last.coordinate.latitude
            val lon = last.coordinate.longitude
            val acc = last.horizontalAccuracy.toFloat()
            val ts = (last.timestamp.timeIntervalSince1970 * 1000).toLong()
            CoroutineScope(Dispatchers.Default).launch {
                _updates.emit(Location(lat, lon, acc, ts))
            }
        }
    }

    init {
        locationManager.delegate = delegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.allowsBackgroundLocationUpdates = true
        locationManager.pausesLocationUpdatesAutomatically = false
    }

    actual fun initialize(platformContext: Any?) { /* no-op on iOS */ }

    actual fun start() {
        locationManager.requestAlwaysAuthorization()
        locationManager.startUpdatingLocation()
    }

    actual fun stop() {
        locationManager.stopUpdatingLocation()
    }
}
