package com.vodang.greenmind.store

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.vodang.greenmind.api.bill.BillItem
import com.vodang.greenmind.api.invoicepollution.InvoicePollutionResponse
import com.vodang.greenmind.time.currentTimeMillis

data class BillRecord(
    val id: String,
    val storeName: String,
    val totalAmount: Double,
    val greenAmount: Double,
    val greenRatio: Int,
    val timestampMillis: Long,
    val items: List<BillItem> = emptyList(),
    val pollutionResult: InvoicePollutionResponse? = null,
    val imageUrl: String? = null,
)

object BillStore {
    private val _bills = MutableStateFlow<List<BillRecord>>(emptyList())
    val bills: StateFlow<List<BillRecord>> = _bills.asStateFlow()

    fun add(storeName: String, totalAmount: Double, greenAmount: Double, greenRatio: Int, items: List<BillItem> = emptyList(), pollutionResult: InvoicePollutionResponse? = null, imageUrl: String? = null) {
        val record = BillRecord(
            id              = kotlin.random.Random.nextLong().toString(),
            storeName       = storeName,
            totalAmount     = totalAmount,
            greenAmount     = greenAmount,
            greenRatio      = greenRatio,
            timestampMillis = currentTimeMillis(),
            items           = items,
            pollutionResult = pollutionResult,
            imageUrl        = imageUrl,
        )
        _bills.value = listOf(record) + _bills.value
    }
}
