package com.vodang.greenmind.wastereport

import androidx.compose.runtime.Composable

/** All data collected during the report flow, ready to send to the API. */
data class WasteReportFormData(
    val imageKey: String,
    val imageUrl: String,
    val wasteType: String,
    val wasteKg: Double,
    val description: String,
    val lat: Double,
    val lng: Double,
    val wardName: String,
)

@Composable
expect fun WasteReportScanScreen(
    onReported: (WasteReportFormData) -> Unit,
    onBack: () -> Unit,
)
