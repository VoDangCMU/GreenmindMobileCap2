package com.vodang.greenmind.api.bill

import com.vodang.greenmind.api.ocr.OcrResponse
import com.vodang.greenmind.api.ocr.ocrBill

// ── App-facing data model ─────────────────────────────────────────────────────
// Used by BillStore and BillListScreen to persist and display scan history.

data class BillItem(val name: String, val amount: Double, val isGreen: Boolean)

data class BillAnalysisResult(
    val storeName: String,
    val totalAmount: Double,
    val greenAmount: Double,
    val greenRatio: Int,
    val items: List<BillItem>,
)

// ── Mapping ───────────────────────────────────────────────────────────────────

/**
 * Maps the raw OCR response to the simplified [BillAnalysisResult] used by the
 * rest of the app (BillStore, BillListScreen, green-ratio feedback).
 *
 * Green ratio = plant-based line-totals / grand_total × 100.
 */
fun OcrResponse.toBillAnalysisResult(): BillAnalysisResult {
    val grandTotal  = totals?.grandTotal ?: totals?.subtotal ?: 0.0
    val plantTotal  = items
        ?.filter { it.plantBased == true }
        ?.sumOf { it.lineTotal ?: 0.0 }
        ?: 0.0
    val ratio = if (grandTotal > 0) ((plantTotal / grandTotal) * 100).toInt().coerceIn(0, 100) else 0

    return BillAnalysisResult(
        storeName   = vendor?.name ?: "",
        totalAmount = grandTotal,
        greenAmount = plantTotal,
        greenRatio  = ratio,
        items = items?.map { item ->
            BillItem(
                name    = buildString {
                    append(item.rawName ?: "Unknown")
                    if (!item.brand.isNullOrBlank()) append(" · ${item.brand}")
                },
                amount  = item.lineTotal ?: 0.0,
                isGreen = item.plantBased == true,
            )
        } ?: emptyList(),
    )
}

// ── Public API ────────────────────────────────────────────────────────────────

/**
 * Convenience wrapper used by callers that only need [BillAnalysisResult]
 * and don't need the full [OcrResponse] detail (e.g. iOS stub, tests).
 *
 * For the Android scan screen use [ocrBill] directly so the rich fields
 * (datetime, totals breakdown, per-item quantity/price) are available.
 */
suspend fun analyzeBill(accessToken: String, imageBytes: ByteArray): BillAnalysisResult =
    ocrBill(accessToken, imageBytes, "bill_${imageBytes.size}.jpg").toBillAnalysisResult()
