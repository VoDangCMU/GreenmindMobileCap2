package com.vodang.greenmind.store

import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.wastesort.WasteSortEntry
import com.vodang.greenmind.wastesort.WasteSortStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.MainScope

object WasteSortStore {
    val storeScope = MainScope()
    private val _entries = MutableStateFlow<List<WasteSortEntry>>(emptyList())
    val entries: StateFlow<List<WasteSortEntry>> = _entries.asStateFlow()

    private val _refreshTrigger = MutableStateFlow(0)
    val refreshTrigger: StateFlow<Int> = _refreshTrigger.asStateFlow()

    fun triggerRefresh() {
        _refreshTrigger.value += 1
    }

    fun add(entry: WasteSortEntry) {
        _entries.value = listOf(entry) + _entries.value
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
}
