package com.vodang.greenmind.wastesort

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
import com.vodang.greenmind.api.envimpact.logEnvironmentalImpact
import com.vodang.greenmind.api.wastedetect.detectWaste
import com.vodang.greenmind.api.wastesort.DetectTrashResponse
import com.vodang.greenmind.api.wastesort.detectTrash
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.launch
import java.io.File

private val green600 = Color(0xFF388E3C)
private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

private enum class WasteSortScanPhase { IDLE, ANALYZING, ERROR }

private fun createScanPhotoUri(context: android.content.Context): Uri {
    val dir  = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "waste_sort_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
actual fun WasteSortScanScreen(
    onResult: (DetectTrashResponse) -> Unit,
    onBack: () -> Unit,
    useGallery: Boolean,
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var phase         by remember { mutableStateOf(WasteSortScanPhase.IDLE) }
    var errorMsg      by remember { mutableStateOf<String?>(null) }
    var pendingUri    by remember { mutableStateOf<Uri?>(null) }
    var capturedBytes by remember { mutableStateOf<ByteArray?>(null) }

    fun analyze(bytes: ByteArray) {
        phase = WasteSortScanPhase.ANALYZING
        scope.launch {
            try {
                val response = detectTrash(bytes)
                onResult(response)
                // Fire pollutant impact in background after sort result is delivered
                val entryId = response.imageUrl.substringAfterLast("/").substringBeforeLast(".")
                WasteSortStore.storeScope.launch {
                    try {
                        val pollutant = detectWaste(bytes)
                        WasteSortStore.updatePollutant(entryId, pollutant)
                        // Log to backend — fire-and-forget
                        val token = SettingsStore.getAccessToken()
                        AppLogger.i("EnvImpact", "WasteSort background impact log success: id=$entryId pollution=${pollutant.pollution}")
                        if (token != null) {
                            logEnvironmentalImpact(
                                accessToken  = token,
                                pollution    = pollutant.pollution,
                                airImpact    = pollutant.impact.airPollution,
                                waterImpact  = pollutant.impact.waterPollution,
                                soilImpact   = pollutant.impact.soilPollution,
                            )
                        }
                    } catch (e: Throwable) {
                        AppLogger.e("EnvImpact", "WasteSort background impact log failed: ${e::class.simpleName}: ${e.message}")
                    }
                }
            } catch (e: Throwable) {
                errorMsg = e.message ?: "Detection failed"
                phase = WasteSortScanPhase.ERROR
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { saved ->
        if (saved) {
            val uri = pendingUri
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    capturedBytes = bytes
                    analyze(bytes)
                } else {
                    onBack()
                }
            }
        } else {
            onBack()
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                capturedBytes = bytes
                analyze(bytes)
            } else {
                onBack()
            }
        } else {
            onBack()
        }
    }

    fun launchCamera() {
        val uri = createScanPhotoUri(context)
        pendingUri = uri
        cameraLauncher.launch(uri)
    }

    when (phase) {
        WasteSortScanPhase.IDLE -> {
            LaunchedEffect(Unit) {
                if (useGallery) galleryLauncher.launch("image/*") else launchCamera()
            }
            Box(modifier = Modifier.fillMaxSize().background(green50))
        }

        WasteSortScanPhase.ANALYZING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(48.dp),
                        strokeWidth = 3.dp,
                    )
                    Text(
                        "Detecting waste…",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        "This may take a few seconds",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                    )
                }
            }
        }

        WasteSortScanPhase.ERROR -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text("⚠️", fontSize = 48.sp)
                    Text(
                        "Detection failed",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B),
                    )
                    Text(
                        errorMsg ?: "Unknown error",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onBack,
                            shape = RoundedCornerShape(12.dp),
                        ) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val bytes = capturedBytes
                                if (bytes != null) analyze(bytes) else phase = WasteSortScanPhase.IDLE
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = green800),
                        ) {
                            Text("Retry")
                        }
                    }
                    TextButton(onClick = { phase = WasteSortScanPhase.IDLE }) {
                        Text("Retake photo", color = green600)
                    }
                }
            }
        }
    }
}
