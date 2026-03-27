package com.vodang.greenmind.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BillRecord(
    val id: String,
    val storeName: String,
    val totalAmount: Double,
    val greenAmount: Double,
    val greenRatio: Int,
    val timestampMillis: Long,
)

object BillStore {
    private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
    val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

    fun add(storeName: String, totalAmount: Double, greenAmount: Double, greenRatio: Int) {
        val record = BillRecord(
            id = kotlin.random.Random.nextLong().toString(),
            storeName = storeName,
            totalAmount = totalAmount,
            greenAmount = greenAmount,
            greenRatio = greenRatio,
            timestampMillis = System.currentTimeMillis(),
        )
        _bills.value = listOf(record) + _bills.value
    }
}
