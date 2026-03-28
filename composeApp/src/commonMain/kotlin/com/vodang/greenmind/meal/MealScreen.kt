package com.vodang.greenmind.meal

import androidx.compose.runtime.*
import com.vodang.greenmind.store.MealStore

@Composable
fun MealScreen() {
    var showScan by remember { mutableStateOf(false) }

    if (showScan) {
        MealScanScreen(
            onScanComplete = { ratio, desc, imageUrl ->
                MealStore.add(ratio, desc, imageUrl)
                showScan = false
            },
            onBack = { showScan = false }
        )
    } else {
        MealListScreen(onScanClick = { showScan = true })
    }
}
