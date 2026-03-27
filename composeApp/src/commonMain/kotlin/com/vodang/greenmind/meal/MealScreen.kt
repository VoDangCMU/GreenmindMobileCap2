package com.vodang.greenmind.meal

import androidx.compose.runtime.*
import com.vodang.greenmind.store.MealStore

@Composable
fun MealScreen(onBack: () -> Unit) {
    var showScan by remember { mutableStateOf(false) }

    if (showScan) {
        MealScanScreen(
            onScanComplete = { ratio, desc ->
                MealStore.add(ratio, desc)
                showScan = false
            },
            onBack = { showScan = false }
        )
    } else {
        MealListScreen(
            onScanClick = { showScan = true },
            onBack = onBack
        )
    }
}
