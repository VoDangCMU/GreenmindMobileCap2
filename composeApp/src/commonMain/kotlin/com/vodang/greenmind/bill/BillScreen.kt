package com.vodang.greenmind.bill

import androidx.compose.runtime.*
import com.vodang.greenmind.store.BillStore

@Composable
fun BillScreen(onBack: () -> Unit) {
    var showScan by remember { mutableStateOf(false) }

    if (showScan) {
        BillScanScreen(
            onScanComplete = { result, storeName ->
                BillStore.add(storeName, result.totalAmount, result.greenAmount, result.greenRatio)
                showScan = false
            },
            onBack = { showScan = false }
        )
    } else {
        BillListScreen(
            onScanClick = { showScan = true },
            onBack = onBack
        )
    }
}
