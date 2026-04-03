package com.vodang.greenmind.wastesort

import androidx.compose.runtime.Composable
import com.vodang.greenmind.api.wastesort.DetectTrashResponse

@Composable
expect fun WasteSortScanScreen(
    onResult: (DetectTrashResponse) -> Unit,
    onBack: () -> Unit,
    useGallery: Boolean = false,
)
