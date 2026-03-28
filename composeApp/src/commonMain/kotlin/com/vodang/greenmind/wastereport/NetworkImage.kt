package com.vodang.greenmind.wastereport

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Platform-specific composable that loads and displays an image from a URL. */
@Composable
expect fun NetworkImage(url: String, modifier: Modifier = Modifier)
