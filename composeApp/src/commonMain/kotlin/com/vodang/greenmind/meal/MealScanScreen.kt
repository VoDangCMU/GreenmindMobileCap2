package com.vodang.greenmind.meal

import androidx.compose.runtime.Composable

@Composable
expect fun MealScanScreen(
    onScanComplete: (plantRatio: Int, description: String) -> Unit,
    onBack: () -> Unit,
)
