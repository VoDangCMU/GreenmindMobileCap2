package com.vodang.greenmind.home.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.api.wastecollector.CollectorCheckinRequest
import com.vodang.greenmind.api.wastecollector.checkinPickup
import com.vodang.greenmind.permission.CameraPermissionGate
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.launch
import java.io.File

private enum class Phase { IDLE, UPLOADING, PATCHING, ERROR }

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
    CameraPermissionGate(onDenied = onDismiss) {
    CheckInScanContent(reportId = reportId, accessToken = accessToken, onSuccess = onSuccess, onDismiss = onDismiss)
    }
}

@Composable
private fun CheckInScanContent(
    reportId: String?,
    accessToken: String,
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var phase      by remember { mutableStateOf(Phase.IDLE) }
    var errorMsg   by remember { mutableStateOf("") }
    var pendingUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher must be registered unconditionally (always in the composition tree).
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        if (!saved) { onDismiss(); return@rememberLauncherForActivityResult }
        val uri = pendingUri ?: run { onDismiss(); return@rememberLauncherForActivityResult }

        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes == null) {
            errorMsg = "Failed to read photo"
            phase = Phase.ERROR
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            try {
                phase = Phase.UPLOADING
                AppLogger.i("CheckIn", "Uploading evidence for report $reportId")
                val filename = "checkin_${System.currentTimeMillis()}.jpg"
                val upload   = requestAndUpload(accessToken, filename, bytes, "image/jpeg")

                phase = Phase.PATCHING
                AppLogger.i("CheckIn", "Calling checkinPickup report=$reportId url=${upload.imageUrl}")
                checkinPickup(
                    accessToken = accessToken,
                    id = reportId!!,
                    request = CollectorCheckinRequest(imageUrl = upload.imageUrl),
                )

                AppLogger.i("CheckIn", "Check-in complete for report $reportId")
                phase = Phase.IDLE
                onSuccess()
            } catch (e: Throwable) {
                AppLogger.e("CheckIn", "Check-in failed: ${e.message}")
                errorMsg = e.message ?: "Unknown error"
                phase = Phase.ERROR
            }
        }
    }

    // Launch camera when reportId becomes non-null (key change fires after launcher is registered).
    LaunchedEffect(reportId) {
        if (reportId == null) {
            phase = Phase.IDLE  // reset when dismissed
            return@LaunchedEffect
        }
        val uri = createPhotoUri(context)
        pendingUri = uri
        cameraLauncher.launch(uri)
    }

    // Show overlay only while active
    if (reportId != null && phase != Phase.IDLE) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.75f)),
            contentAlignment = Alignment.Center,
        ) {
            when (phase) {
                Phase.UPLOADING, Phase.PATCHING -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        CircularProgressIndicator(color = Color.White)
                        Text(
                            if (phase == Phase.UPLOADING) "⬆  Uploading photo…" else "✅  Saving check-in…",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
                Phase.ERROR -> {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text("⚠", fontSize = 40.sp)
                        Text(
                            errorMsg,
                            color = Color.White,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                        )
                        TextButton(onClick = onDismiss) {
                            Text("Dismiss", color = Color(0xFFFFB300), fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                else -> {}
            }
        }
    }
}
