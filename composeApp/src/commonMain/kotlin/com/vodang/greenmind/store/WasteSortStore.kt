package com.vodang.greenmind.store

import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.households.getDetectHistoryByUser
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.wastesort.WasteSortEntry
import com.vodang.greenmind.wastesort.WasteSortStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

object WasteSortStore {
    val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _entries = MutableStateFlow<List<WasteSortEntry>>(emptyList())
    val entries: StateFlow<List<WasteSortEntry>> = _entries.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    /** Total kg of today's waste entries from API */
    private val _todayTotalKg = MutableStateFlow(0.0)
    val todayTotalKg: StateFlow<Double> = _todayTotalKg.asStateFlow()

    fun loadFromApi() {
        val token = SettingsStore.getAccessToken() ?: return
        storeScope.launch {
            try {
                val history = getDetectHistoryByUser(token).data
                val today = LocalDate.now().toString()
                // Map API entries to WasteSortEntry
                val entries = history.map { dto ->
                    WasteSortEntry(
                        id = dto.id,
                        backendId = dto.id,
                        imageUrl = dto.imageUrl,
                        totalObjects = dto.totalObjects ?: 0,
                        grouped = emptyMap(), // API doesn't return grouped
                        createdAt = dto.createdAt?.take(10) ?: today,
                        scannedBy = dto.detectedBy?.fullName ?: "Me",
                        status = when (dto.status) {
                            "detected" -> WasteSortStatus.SCANNED
                            "brought_out" -> WasteSortStatus.BRINGOUTED
                            "picked_up", "collected" -> WasteSortStatus.COLLECTED
                            else -> WasteSortStatus.SCANNED
                        },
                        totalMassKg = dto.totalMassKg,
                    )
                }
                _entries.value = entries
                // Calculate today's total kg
                _todayTotalKg.value = entries
                    .filter { it.createdAt.startsWith(today) }
                    .sumOf { it.totalMassKg ?: 0.0 }
            } catch (e: Throwable) {
                // Silent fail
            }
        }
    }

    fun triggerRefresh() {
        _refreshTrigger.value += 1
        loadFromApi()
    }

    fun add(entry: WasteSortEntry) {
        _entries.value = listOf(entry) + _entries.value
        updateTodayKg()
    }

    private fun updateTodayKg() {
        val today = LocalDate.now().toString()
        _todayTotalKg.value = _entries.value
            .filter { it.createdAt.startsWith(today) }
            .sumOf { it.totalMassKg ?: 0.0 }
    }

    fun updateStatus(id: String, status: WasteSortStatus) {
        _entries.value = _entries.value.map { entry ->
            if (entry.id == id) entry.copy(status = status) else entry
        }
    }

    fun updatePollutant(id: String, pollutant: WasteDetectResponse) {
        _entries.value = _entries.value.map { entry ->
            if (entry.id == id) entry.copy(pollutantResult = pollutant) else entry
        }
    }

    fun updateGreenScore(id: String, greenScore: GreenScoreEntryDto) {
        _entries.value = _entries.value.map { entry ->
            if (entry.id == id) entry.copy(greenScoreResult = greenScore) else entry
        }
    }
}
