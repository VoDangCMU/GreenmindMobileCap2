package com.vodang.greenmind.wastereport

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import android.graphics.BitmapFactory
import androidx.compose.foundation.text.KeyboardOptions
import com.vodang.greenmind.api.nominatim.ReverseOptions
import com.vodang.greenmind.api.nominatim.nominatimReverse
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val red      = Color(0xFFC62828)

private enum class WasteReportScanPhase { IDLE, CAPTURED, FORM, UPLOADING }

private val wasteTypeOptions = listOf(
    "plastic"  to "🧴  Plastic",
    "organic"  to "🍌  Organic",
    "metal"    to "🔩  Metal",
    "glass"    to "🫙  Glass",
    "mixed"    to "🗑️  Mixed",
    "other"    to "❓  Other",
)

private fun createWastePhotoUri(context: android.content.Context): Uri {
    val dir = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "waste_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
actual fun WasteReportScanScreen(
    onReported: (WasteReportFormData) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phase by remember { mutableStateOf(WasteReportScanPhase.IDLE) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Form fields
    var selectedWasteType by remember { mutableStateOf("mixed") }
    var wasteKgText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var wardName by remember { mutableStateOf("") }

    // GPS + reverse-geocode state
    var currentLat by remember { mutableStateOf(0.0) }
    var currentLng by remember { mutableStateOf(0.0) }
    var isLoadingLocation by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) {
            val uri = photoUri
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    capturedBytes = bytes
                    phase = WasteReportScanPhase.CAPTURED
                }
            }
        } else if (phase == WasteReportScanPhase.IDLE) {
            onBack()
        }
    }

    when (phase) {
        // ── Step 1: Camera ───────────────────────────────────────────────────
        WasteReportScanPhase.IDLE -> {
            LaunchedEffect(Unit) {
                error = null
                val uri = createWastePhotoUri(context)
                photoUri = uri
                cameraLauncher.launch(uri)
            }
            Box(modifier = Modifier.fillMaxSize().background(green50))
        }

        // ── Step 2: Preview ──────────────────────────────────────────────────
        WasteReportScanPhase.CAPTURED -> {
            val bytes = capturedBytes
            Box(modifier = Modifier.fillMaxSize()) {
                if (bytes != null) {
                    val bitmap = remember(bytes) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                    }
                    bitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.60f))
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = {
                                capturedBytes = null
                                val uri = createWastePhotoUri(context)
                                photoUri = uri
                                cameraLauncher.launch(uri)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("🔄  ${s.wasteReportRetake}")
                        }
                        Button(
                            onClick = { phase = WasteReportScanPhase.FORM },
                            colors = ButtonDefaults.buttonColors(containerColor = green800),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("→  ${s.wasteReportContinue}", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // ── Step 3: Fill in report details ───────────────────────────────────
        WasteReportScanPhase.FORM -> {
            val bytes = capturedBytes

            // Auto-fill wardName from GPS on first entry
            LaunchedEffect(Unit) {
                isLoadingLocation = true
                try {
                    val loc = withTimeoutOrNull(5_000L) { Geo.service.locationUpdates.first() }
                    if (loc != null) {
                        currentLat = loc.latitude
                        currentLng = loc.longitude
                        val place = nominatimReverse(
                            loc.latitude, loc.longitude,
                            ReverseOptions(zoom = 18, addressDetails = true, language = "vi")
                        )
                        val suburb = place.address?.suburb ?: place.address?.neighbourhood
                        if (!suburb.isNullOrBlank() && wardName.isBlank()) {
                            wardName = suburb
                        }
                    }
                } catch (_: Throwable) {
                    // silent — user can type manually
                } finally {
                    isLoadingLocation = false
                }
            }

            Column(modifier = Modifier.fillMaxSize().background(green50)) {
                // In-content back button to return to photo preview
                TextButton(
                    onClick = { phase = WasteReportScanPhase.CAPTURED },
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                ) {
                    Text("← ${s.back}", color = green800, fontSize = 14.sp)
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Photo thumbnail
                    if (bytes != null) {
                        val bitmap = remember(bytes) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                        }
                        bitmap?.let {
                            Image(
                                bitmap = it,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }

                    // Waste type picker
                    Text(
                        s.wasteReportLabel,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242)
                    )
                    val rows = wasteTypeOptions.chunked(3)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        rows.forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { (value, label) ->
                                    val selected = selectedWasteType == value
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (selected) green800 else Color.White)
                                            .border(
                                                width = if (selected) 0.dp else 1.dp,
                                                color = Color(0xFFE0E0E0),
                                                shape = RoundedCornerShape(10.dp)
                                            )
                                            .clickable { selectedWasteType = value }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            fontSize = 12.sp,
                                            color = if (selected) Color.White else Color(0xFF424242),
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                                // Pad last row if uneven
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }

                    // Estimated weight
                    OutlinedTextField(
                        value = wasteKgText,
                        onValueChange = { wasteKgText = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text(s.wasteReportWeightLabel) },
                        placeholder = { Text("e.g. 5", color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor = green800,
                            cursorColor = green800
                        )
                    )

                    // Ward / location name (auto-filled from GPS reverse-geocode)
                    OutlinedTextField(
                        value = wardName,
                        onValueChange = { wardName = it },
                        label = { Text(s.wasteReportWardLabel) },
                        placeholder = {
                            // TODO: Move "Đang lấy vị trí…" to i18n AppStrings (wasteReportLocating).
                            if (isLoadingLocation) Text("Đang lấy vị trí…", color = Color.LightGray)
                            else Text("e.g. Phường Hải Châu 1", color = Color.LightGray)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        trailingIcon = {
                            if (isLoadingLocation) CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = green600,
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor = green800,
                            cursorColor = green800
                        )
                    )

                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text(s.wasteReportNoteLabel) },
                        placeholder = { Text(s.wasteReportNoteHint, color = Color.LightGray) },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor = green800,
                            cursorColor = green800
                        )
                    )

                    error?.let { msg ->
                        Text(msg, color = red, fontSize = 13.sp)
                    }
                }

                // Submit button
                Button(
                    onClick = {
                        val kg = wasteKgText.toDoubleOrNull() ?: 0.0
                        phase = WasteReportScanPhase.UPLOADING
                        scope.launch {
                            val token = SettingsStore.getAccessToken()
                            if (token == null || bytes == null) {
                                error = s.wasteReportUploadError
                                phase = WasteReportScanPhase.FORM
                                return@launch
                            }
                            try {
                                val filename = "waste_${System.currentTimeMillis()}.jpg"
                                val result = requestAndUpload(token, filename, bytes, "image/jpeg")
                                AppLogger.i("WasteReport", "Uploaded key=${result.key}")
                                onReported(
                                    WasteReportFormData(
                                        imageKey = result.key,
                                        imageUrl = result.imageUrl,
                                        wasteType = selectedWasteType,
                                        wasteKg = kg,
                                        description = description.ifBlank { wasteTypeOptions.find { it.first == selectedWasteType }?.second?.trim() ?: selectedWasteType },
                                        lat = currentLat,
                                        lng = currentLng,
                                        wardName = wardName.ifBlank { "Unknown" },
                                    )
                                )
                            } catch (e: Throwable) {
                                AppLogger.e("WasteReport", "Upload failed: ${e.message}")
                                error = s.wasteReportUploadError
                                phase = WasteReportScanPhase.FORM
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                    colors = ButtonDefaults.buttonColors(containerColor = green800),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("✅  ${s.wasteReportSubmit}", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        }

        // ── Step 4: Uploading ────────────────────────────────────────────────
        WasteReportScanPhase.UPLOADING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text(s.wasteReportUploading, color = Color.White, fontSize = 15.sp)
                }
            }
        }
    }
}
