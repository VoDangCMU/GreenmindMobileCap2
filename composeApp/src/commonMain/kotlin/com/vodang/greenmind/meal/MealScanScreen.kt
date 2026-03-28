package com.vodang.greenmind.meal

import androidx.compose.runtime.Composable

@Composable
expect fun MealScanScreen(
    onScanComplete: (plantRatio: Int, description: String, imageUrl: String?) -> Unit,
    onBack: () -> Unit,
)
