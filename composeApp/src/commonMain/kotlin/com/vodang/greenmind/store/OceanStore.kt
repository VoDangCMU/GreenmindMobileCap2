package com.vodang.greenmind.store

import com.vodang.greenmind.api.ocean.CreateOceanRequest
import com.vodang.greenmind.api.ocean.OceanScores
import com.vodang.greenmind.api.ocean.OceanScoresInput
import com.vodang.greenmind.api.ocean.UpdateOceanRequest
import com.vodang.greenmind.api.ocean.createOcean
import com.vodang.greenmind.api.ocean.getOcean
import com.vodang.greenmind.api.ocean.updateOcean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private val DEFAULT_SCORES = OceanScoresInput(O = 50.0, C = 50.0, E = 50.0, A = 50.0, N = 50.0)

object OceanStore {

    private val scope = CoroutineScope(SupervisorJob())

    private val _scores = MutableStateFlow<OceanScores?>(null)
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val scores: StateFlow<OceanScores?> = _scores.asStateFlow()
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()

    /**
     * Fetches the ocean scores for the current user.
     * If the fetch fails (e.g. not found), creates a record with default scores then re-fetches.
     */
    fun load() {
        val token = SettingsStore.getAccessToken() ?: return
        val userId = SettingsStore.getUser()?.id ?: return
        scope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _scores.value = getOcean(token, userId).scores
            } catch (_: Throwable) {
                // Record doesn't exist — create with defaults then re-fetch
                try {
                    createOcean(token, CreateOceanRequest(userId = userId, scores = DEFAULT_SCORES))
                    _scores.value = getOcean(token, userId).scores
                } catch (e: Throwable) {
                    _error.value = e.message
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun update(input: OceanScoresInput) {
        val token = SettingsStore.getAccessToken() ?: return
        val userId = SettingsStore.getUser()?.id ?: return
        scope.launch {
            try {
                val resp = updateOcean(token, userId, UpdateOceanRequest(input))
                _scores.value = resp.data.scores
            } catch (e: Throwable) {
                _error.value = e.message
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}
