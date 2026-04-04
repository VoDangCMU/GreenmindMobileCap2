package com.vodang.greenmind.home.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File

private fun createPhotoUri(context: android.content.Context): Uri {
    val dir  = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "checkin_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
actual fun CheckInScanFlow(
    reportId: String?,
    accessToken: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        if (saved) onSuccess() else onDismiss()
        pendingUri = null
    }

    LaunchedEffect(reportId) {
        if (reportId == null) return@LaunchedEffect
        val uri = createPhotoUri(context)
        pendingUri = uri
        cameraLauncher.launch(uri)
    }
}
