package com.vodang.greenmind.store

import com.vodang.greenmind.api.households.HouseholdDto
import com.vodang.greenmind.api.households.getCurrentUserHousehold
import com.vodang.greenmind.api.households.createHousehold
import com.vodang.greenmind.api.households.CreateHouseholdRequest
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object HouseholdStore {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _household = MutableStateFlow<HouseholdDto?>(null)
    val household: StateFlow<HouseholdDto?> = _household.asStateFlow()

    private val _isFetching = MutableStateFlow(false)
    val isFetching: StateFlow<Boolean> = _isFetching.asStateFlow()

    /** True once the first fetch attempt has completed (success or failure). */
    private val _hasFetched = MutableStateFlow(false)
    val hasFetched: StateFlow<Boolean> = _hasFetched.asStateFlow()

    fun fetchHousehold() {
        if (_isFetching.value) return  // already in flight

        val token = SettingsStore.getAccessToken()
        if (token == null) {
            _household.value = null
            _hasFetched.value = true
            return
        }

        _hasFetched.value = false
        _isFetching.value = true
        scope.launch {
            try {
                val response = getCurrentUserHousehold(token)
                _household.value = response.data.toHouseholdDto()
                AppLogger.i("HouseholdStore", "Household fetched successfully")
            } catch (e: Exception) {
                // Usually a 404 or network error
                _household.value = null
                AppLogger.e("HouseholdStore", "No household found or error: ${e.message}")
            } finally {
                _isFetching.value = false
                _hasFetched.value = true
            }
        }
    }

    fun clearHousehold() {
        _household.value = null
    }

    fun setHousehold(household: HouseholdDto) {
        _household.value = household
        _hasFetched.value = true
        _isFetching.value = false
    }

    suspend fun createNewHousehold(address: String, lat: Double, lng: Double): Boolean {
        val token = SettingsStore.getAccessToken() ?: return false
        return try {
            createHousehold(token, CreateHouseholdRequest(address, lat, lng))
            AppLogger.i("HouseholdStore", "Created household successfully")
            fetchHousehold() // refresh state after creation
            true
        } catch (e: Exception) {
            AppLogger.e("HouseholdStore", "Failed to create household: ${e.message}")
            false
        }
    }
}
