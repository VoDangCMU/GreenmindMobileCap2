package com.vodang.greenmind.home.components

import androidx.compose.runtime.Composable

@Composable
actual fun CheckInScanFlow(
    reportId: String?,
    accessToken: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    // Check-in camera scan not available on desktop — no-op
}
