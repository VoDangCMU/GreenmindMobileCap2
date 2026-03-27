package com.vodang.greenmind.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NetworkEntry(
    val method: String,
    val url: String,
    val statusCode: Int = 0,
    val durationMs: Long = 0,
    val requestHeaders: List<Pair<String, String>> = emptyList(),
    val requestBody: String = "",
    val responseHeaders: List<Pair<String, String>> = emptyList(),
    val responseBody: String = "",
)

object NetworkCaptureStore {
    private val _entries = MutableStateFlow<List<NetworkEntry>>(emptyList())
    val entries: StateFlow<List<NetworkEntry>> = _entries.asStateFlow()

    fun add(entry: NetworkEntry) {
        _entries.value = (_entries.value + entry).takeLast(100)
    }

    fun clear() {
        _entries.value = emptyList()
    }
}
