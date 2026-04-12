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
import com.vodang.greenmind.permission.CameraPermissionGate
import com.vodang.greenmind.api.envimpact.logEnvironmentalImpact
import com.vodang.greenmind.api.households.DetectImageUrlRequest
import com.vodang.greenmind.api.households.detectTrashOnly
import com.vodang.greenmind.api.households.predictPollutant
import com.vodang.greenmind.api.households.detectTotalMass
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.api.wastedetect.WasteDetectImpact
import com.vodang.greenmind.api.wastedetect.WasteDetectItem
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.api.wastesort.DetectTrashResponse
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
    CameraPermissionGate(onDenied = onBack, skip = useGallery) {
    WasteSortScanContent(onResult = onResult, onBack = onBack, useGallery = useGallery)
    }
}

@Composable
private fun WasteSortScanContent(
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
            val token = SettingsStore.getAccessToken()
            if (token == null) {
                errorMsg = "Sign in required to scan waste"
                phase = WasteSortScanPhase.ERROR
                return@launch
            }
            try {
                // 1. Upload image so the backend can process it by URL
                val upload  = requestAndUpload(
                    accessToken = token,
                    filename    = "waste_scan_${System.currentTimeMillis()}.jpg",
                    fileBytes   = bytes,
                    contentType = "image/jpeg",
                )
                val request = DetectImageUrlRequest(imageUrl = upload.imageUrl)

                // 2. Primary scan via backend /households/detect-trash
                val detectResult = detectTrashOnly(token, request)
                val dto          = detectResult.data
                val displayUrl   = dto.annotatedImageUrl ?: dto.aiAnalysis ?: dto.imageUrl

                // Build grouped from item names (backend has item list, not per-item segments)
                val grouped = dto.items
                    ?.groupBy { it.name }
                    ?.mapValues { listOf(displayUrl) }
                    ?: emptyMap()

                val response = DetectTrashResponse(
                    totalObjects = dto.totalObjects ?: 0,
                    imageUrl     = displayUrl,
                    grouped      = grouped,
                )
                onResult(response)

                val entryId = displayUrl.substringAfterLast("/").substringBeforeLast(".")

                // 3. predict-pollutant + total-mass in background
                WasteSortStore.storeScope.launch {
                    try {
                        val result    = predictPollutant(token, request)
                        val pollDto   = result.data
                        val pollution = pollDto.pollution
                        val impact    = pollDto.impact
                        if (pollution != null && impact != null) {
                            val pollutionMap = buildMap<String, Double> {
                                pollution.cd?.let               { put("Cd", it.toDouble()) }
                                pollution.hg?.let               { put("Hg", it.toDouble()) }
                                pollution.pb?.let               { put("Pb", it.toDouble()) }
                                pollution.ch4?.let              { put("CH4", it.toDouble()) }
                                pollution.co2?.let              { put("CO2", it) }
                                pollution.nox?.let              { put("NOx", it) }
                                pollution.so2?.let              { put("SO2", it) }
                                pollution.pm25?.let             { put("PM2.5", it.toDouble()) }
                                pollution.dioxin?.let           { put("dioxin", it) }
                                pollution.nitrate?.let          { put("nitrate", it.toDouble()) }
                                pollution.styrene?.let          { put("styrene", it) }
                                pollution.microplastic?.let     { put("microplastic", it) }
                                pollution.toxicChemicals?.let   { put("toxic_chemicals", it) }
                                pollution.chemicalResidue?.let  { put("chemical_residue", it.toDouble()) }
                                pollution.nonBiodegradable?.let { put("non_biodegradable", it) }
                            }
                            WasteSortStore.updatePollutant(
                                entryId,
                                WasteDetectResponse(
                                    items        = pollDto.items?.map { WasteDetectItem(it.name, it.quantity, it.area) } ?: emptyList(),
                                    totalObjects = pollDto.totalObjects ?: 0,
                                    imageUrl     = pollDto.aiAnalysis ?: pollDto.imageUrl,
                                    pollution    = pollutionMap,
                                    impact       = WasteDetectImpact(
                                        airPollution   = impact.airPollution ?: 0.0,
                                        waterPollution = impact.waterPollution ?: 0.0,
                                        soilPollution  = impact.soilPollution ?: 0.0,
                                    ),
                                )
                            )
                            logEnvironmentalImpact(
                                accessToken = token,
                                pollution   = pollutionMap,
                                airImpact   = impact.airPollution ?: 0.0,
                                waterImpact = impact.waterPollution ?: 0.0,
                                soilImpact  = impact.soilPollution ?: 0.0,
                            )
                        }
                        AppLogger.i("HouseholdScan", "predictPollutant ok id=${pollDto.id}")
                    } catch (e: Throwable) {
                        AppLogger.e("HouseholdScan", "predictPollutant failed: ${e.message}")
                    }

                    try {
                        val massResult = detectTotalMass(token, request)
                        AppLogger.i("HouseholdScan", "detectTotalMass ok id=${massResult.data.id} mass=${massResult.data.totalMassKg}")
                    } catch (e: Throwable) {
                        AppLogger.e("HouseholdScan", "detectTotalMass failed: ${e.message}")
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
