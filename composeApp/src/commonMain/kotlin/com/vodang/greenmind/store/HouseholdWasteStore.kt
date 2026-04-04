package com.vodang.greenmind.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class WasteScanRecord(
    val id: String,
    val totalObjects: Int,
    val imageUrl: String,
    val segments: List<String>,
    val wasteCategory: String,
    val status: String,   // SCANNED_RAW → SCANNED_SORTED → DISPOSED → PROCESSED
    val scannedAt: String,
    val co2AvoidedKg: Double,
)

object HouseholdWasteStore {
    private val _records = MutableStateFlow<List<WasteScanRecord>>(emptyList())
    val records: StateFlow<List<WasteScanRecord>> = _records.asStateFlow()

    fun add(record: WasteScanRecord) {
        _records.value = listOf(record) + _records.value
    }

    fun updateStatus(id: String, status: String) {
        _records.value = _records.value.map { if (it.id == id) it.copy(status = status) else it }
    }

    fun markAllProcessed() {
        _records.value = _records.value.map { it.copy(status = "PROCESSED") }
    }
}
