package com.vodang.greenmind.store

import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.households.getDetectsMonthly
import com.vodang.greenmind.api.wastedetect.WasteDetectImpact
import com.vodang.greenmind.api.wastedetect.WasteDetectItem
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
import com.vodang.greenmind.time.todayLocalIsoDate

object WasteSortStore {
    val storeScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _entries = MutableStateFlow<List<WasteSortEntry>>(emptyList())
    val entries: StateFlow<List<WasteSortEntry>> = _entries.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    private val _greenScoreTrigger = MutableStateFlow(0)
    val greenScoreTrigger: StateFlow<Int> = _greenScoreTrigger.asStateFlow()

    /** User scan history from API (all 3 types merged) */
    private val _userHistory = MutableStateFlow<List<DetectTrashHistoryDto>>(emptyList())
    val userHistory: StateFlow<List<DetectTrashHistoryDto>> = _userHistory.asStateFlow()

    /** Total kg of today's waste entries from API */
    private val _todayTotalKg = MutableStateFlow(0.0)
    val todayTotalKg: StateFlow<Double> = _todayTotalKg.asStateFlow()

    fun loadFromApi() {
        val token = SettingsStore.getAccessToken() ?: return
        storeScope.launch {
            try {
                val history = getDetectsMonthly(token).data
                val today = todayLocalIsoDate()
                // Map API entries to WasteSortEntry
                val entries = history.map { dto ->
                    val pollutionMap = dto.pollution?.let { p ->
                        buildMap<String, Double> {
                            p.cd?.let               { put("Cd", it) }
                            p.hg?.let               { put("Hg", it) }
                            p.pb?.let               { put("Pb", it) }
                            p.ch4?.let              { put("CH4", it) }
                            p.co2?.let             { put("CO2", it) }
                            p.nox?.let             { put("NOx", it) }
                            p.so2?.let             { put("SO2", it) }
                            p.pm25?.let            { put("PM2.5", it) }
                            p.dioxin?.let         { put("dioxin", it) }
                            p.nitrate?.let         { put("nitrate", it) }
                            p.styrene?.let         { put("styrene", it) }
                            p.microplastic?.let    { put("microplastic", it) }
                            p.toxicChemicals?.let  { put("toxic_chemicals", it) }
                            p.chemicalResidue?.let { put("chemical_residue", it) }
                            p.nonBiodegradable?.let { put("non_biodegradable", it) }
                        }
                    }
                    val pollutantResult = if (dto.pollution != null && dto.impact != null) {
                        WasteDetectResponse(
                            items = dto.items?.map {
                                WasteDetectItem(it.name, it.quantity, it.area)
                            } ?: emptyList(),
                            totalObjects = dto.totalObjects ?: 0,
                            imageUrl = dto.aiAnalysis ?: dto.annotatedImageUrl ?: dto.imageUrl,
                            pollution = pollutionMap ?: emptyMap(),
                            impact = WasteDetectImpact(
                                airPollution   = dto.impact.airPollution ?: 0.0,
                                waterPollution = dto.impact.waterPollution ?: 0.0,
                                soilPollution  = dto.impact.soilPollution ?: 0.0,
                            ),
                        )
                    } else null
                    WasteSortEntry(
                        id = dto.id,
                        backendId = dto.id,
                        imageUrl = dto.annotatedImageUrl ?: dto.aiAnalysis ?: dto.imageUrl,
                        totalObjects = dto.totalObjects ?: 0,
                        grouped = dto.segments?.let { seg ->
                            buildMap {
                                if (seg.recyclable.isNotEmpty()) put("recyclable", seg.recyclable)
                                if (seg.residual.isNotEmpty()) put("residual", seg.residual)
                            }
                        } ?: emptyMap(),
                        createdAt = dto.createdAt?.take(10) ?: today,
                        scannedBy = dto.detectedBy?.fullName ?: "Me",
                        status = when (dto.status) {
                            "detected" -> WasteSortStatus.SORTED
                            "brought_out" -> WasteSortStatus.BRINGOUTED
                            "picked_up", "collected" -> WasteSortStatus.COLLECTED
                            else -> WasteSortStatus.SORTED
                        },
                        pollutantResult = pollutantResult,
                        totalMassKg = dto.totalMassKg,
                        detectType = dto.detectType,
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

    fun fetchUserScans(token: String) {
        storeScope.launch {
            try {
                val response = getDetectsMonthly(token)
                _userHistory.value = response.data
                _entries.value = response.data.map { dto ->
                    val today = todayLocalIsoDate()
                    val pollutionMap = dto.pollution?.let { p ->
                        buildMap<String, Double> {
                            p.cd?.let               { put("Cd", it) }
                            p.hg?.let               { put("Hg", it) }
                            p.pb?.let               { put("Pb", it) }
                            p.ch4?.let              { put("CH4", it) }
                            p.co2?.let             { put("CO2", it) }
                            p.nox?.let             { put("NOx", it) }
                            p.so2?.let             { put("SO2", it) }
                            p.pm25?.let            { put("PM2.5", it) }
                            p.dioxin?.let         { put("dioxin", it) }
                            p.nitrate?.let         { put("nitrate", it) }
                            p.styrene?.let         { put("styrene", it) }
                            p.microplastic?.let    { put("microplastic", it) }
                            p.toxicChemicals?.let  { put("toxic_chemicals", it) }
                            p.chemicalResidue?.let { put("chemical_residue", it) }
                            p.nonBiodegradable?.let { put("non_biodegradable", it) }
                        }
                    }
                    val pollutantResult = if (dto.pollution != null && dto.impact != null) {
                        WasteDetectResponse(
                            items = dto.items?.map {
                                WasteDetectItem(it.name, it.quantity, it.area)
                            } ?: emptyList(),
                            totalObjects = dto.totalObjects ?: 0,
                            imageUrl = dto.aiAnalysis ?: dto.annotatedImageUrl ?: dto.imageUrl,
                            pollution = pollutionMap ?: emptyMap(),
                            impact = WasteDetectImpact(
                                airPollution   = dto.impact.airPollution ?: 0.0,
                                waterPollution = dto.impact.waterPollution ?: 0.0,
                                soilPollution  = dto.impact.soilPollution ?: 0.0,
                            ),
                        )
                    } else null
                    WasteSortEntry(
                        id = dto.id,
                        backendId = dto.id,
                        imageUrl = dto.annotatedImageUrl ?: dto.aiAnalysis ?: dto.imageUrl,
                        totalObjects = dto.totalObjects ?: 0,
                        grouped = dto.segments?.let { seg ->
                            buildMap {
                                if (seg.recyclable.isNotEmpty()) put("recyclable", seg.recyclable)
                                if (seg.residual.isNotEmpty()) put("residual", seg.residual)
                            }
                        } ?: emptyMap(),
                        createdAt = dto.createdAt?.take(10) ?: today,
                        scannedBy = dto.detectedBy?.fullName ?: "Me",
                        status = when (dto.status) {
                            "detected" -> WasteSortStatus.SORTED
                            "brought_out" -> WasteSortStatus.BRINGOUTED
                            "picked_up", "collected" -> WasteSortStatus.COLLECTED
                            else -> WasteSortStatus.SORTED
                        },
                        pollutantResult = pollutantResult,
                        totalMassKg = dto.totalMassKg,
                        detectType = dto.detectType,
                    )
                }
                _todayTotalKg.value = _entries.value
                    .filter { it.createdAt.startsWith(todayLocalIsoDate()) }
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
        val today = todayLocalIsoDate()
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
        _greenScoreTrigger.value += 1
    }
}
