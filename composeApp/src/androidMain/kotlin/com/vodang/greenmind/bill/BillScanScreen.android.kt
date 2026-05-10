package com.vodang.greenmind.bill

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.vodang.greenmind.api.bill.BillAnalysisResult
import com.vodang.greenmind.api.bill.BillItem
import com.vodang.greenmind.api.bill.toBillAnalysisResult
import com.vodang.greenmind.api.envimpact.logEnvironmentalImpact
import com.vodang.greenmind.api.invoicepollution.InvoicePollutionResponse
import com.vodang.greenmind.api.invoicepollution.scanInvoicePollution
import com.vodang.greenmind.api.invoicepollution.toWasteDetectResponse
import com.vodang.greenmind.api.ocr.OcrResponse
import com.vodang.greenmind.api.ocr.ocrBill
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.home.components.EnvImpactCard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.permission.CameraPermissionGate
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)
private val orange   = Color(0xFFE65100)
private val red      = Color(0xFFC62828)
private val gray50   = Color(0xFFF5F5F5)

private enum class BillScanPhase { IDLE, CAPTURED, ANALYZING, RESULT }

private fun createBillPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "bill_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun InvoicePollutionResponse.toBillAnalysisResult(): BillAnalysisResult = BillAnalysisResult(
    storeName   = "",
    totalAmount = totalItems.toDouble(),
    greenAmount = 0.0,
    greenRatio  = ecoScore,
    items       = items.map { BillItem(name = it.rawName, amount = 0.0, isGreen = false) },
)

@Composable
actual fun BillScanScreen(
    onScanComplete: (result: BillAnalysisResult, storeName: String, imageUrl: String?) -> Unit,
    onBack: () -> Unit,
) {
    CameraPermissionGate(onDenied = onBack) {
    BillScanContent(onScanComplete = onScanComplete, onBack = onBack)
    }
}

@Composable
private fun BillScanContent(
    onScanComplete: (result: BillAnalysisResult, storeName: String, imageUrl: String?) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phase by remember { mutableStateOf(BillScanPhase.IDLE) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var detectResult by remember { mutableStateOf<InvoicePollutionResponse?>(null) }
    var uploadedImageUrl by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) {
            val uri = photoUri ?: return@rememberLauncherForActivityResult
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) { capturedBytes = bytes; phase = BillScanPhase.CAPTURED }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) { capturedBytes = bytes; phase = BillScanPhase.CAPTURED }
        }
    }

    when (phase) {

        // ── Step 1: Choose source ─────────────────────────────────────────────
        BillScanPhase.IDLE -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(green50)
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(s.billScanTitle, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = green800)
                Spacer(Modifier.height(8.dp))
                Text(s.billScanHint, fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(32.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        error = null
                        val uri = createBillPhotoUri(context)
                        photoUri = uri
                        cameraLauncher.launch(uri)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = green800)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(s.billCapture)
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { galleryLauncher.launch("image/*") }
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(s.billUpload)
                }
                Spacer(Modifier.height(12.dp))
                TextButton(modifier = Modifier.fillMaxWidth(), onClick = onBack) {
                    Text(s.back, color = Color.Gray)
                }
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(error!!, color = red, fontSize = 13.sp)
                }
            }
        }

        // ── Step 2: Preview captured image ────────────────────────────────────
        BillScanPhase.CAPTURED -> {
            val bytes = capturedBytes
            Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (bytes != null) {
                        val bitmap = remember(bytes) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
                        }
                        bitmap?.let {
                            Image(
                                bitmap = it,
                                contentDescription = null,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(green50)
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            capturedBytes = null
                            val uri = createBillPhotoUri(context)
                            photoUri = uri
                            cameraLauncher.launch(uri)
                        }
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(s.billRetake)
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            phase = BillScanPhase.ANALYZING
                            scope.launch {
                                try {
                                    val b = capturedBytes!!
                                    val filename = "bill_${System.currentTimeMillis()}.jpg"
                                    val token = SettingsStore.getAccessToken()
                                    coroutineScope {
                                        val detectDeferred = async {
                                            runCatching { scanInvoicePollution(imageBytes = b, filename = filename) }
                                                .onFailure { AppLogger.e("BillScan", "scanInvoicePollution failed: ${it.message}") }
                                                .getOrNull()
                                        }
                                        val uploadDeferred = if (token != null) async {
                                            runCatching {
                                                requestAndUpload(
                                                    accessToken = token,
                                                    filename    = filename,
                                                    fileBytes   = b,
                                                    contentType = "image/jpeg",
                                                ).imageUrl
                                            }.getOrNull()
                                        } else null
                                        detectResult = detectDeferred.await()
                                        uploadedImageUrl = uploadDeferred?.await()
                                    }
                                    val res = detectResult
                                    if (token != null && res != null) {
                                        launch {
                                            runCatching {
                                                logEnvironmentalImpact(
                                                    accessToken = token,
                                                    pollution   = res.pollution,
                                                    airImpact   = res.impact.air,
                                                    waterImpact = res.impact.water,
                                                    soilImpact  = res.impact.soil,
                                                )
                                            }.onFailure { e ->
                                                AppLogger.e("EnvImpact", "Bill impact log failed: ${e::class.simpleName}: ${e.message}")
                                            }
                                        }
                                    }
                                    if (detectResult == null) {
                                        error = s.billError
                                        phase = BillScanPhase.IDLE
                                    } else {
                                        phase = BillScanPhase.RESULT
                                    }
                                } catch (e: Throwable) {
                                    error = s.billError
                                    phase = BillScanPhase.IDLE
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = green800)
                    ) { Text("🔍 ${s.billAnalyze}") }
                }
            }
        }

        // ── Step 3: Waiting ───────────────────────────────────────────────────
        BillScanPhase.ANALYZING -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = Color.White)
                    Text(s.billAnalyzing, color = Color.White, fontSize = 15.sp)
                    Text("This may take up to a minute…", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        }

        // ── Step 4: Show result ───────────────────────────────────────────────
        BillScanPhase.RESULT -> {
            val det = detectResult
            if (det == null) {
                error = s.billError; phase = BillScanPhase.IDLE; return
            }
            var label by remember(det) { mutableStateOf(det.items.firstOrNull()?.rawName ?: "") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gray50)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Label field ───────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text(s.billStoreNameLabel) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor  = green800,
                            cursorColor        = green800,
                        )
                    )
                }

                // ── Receipt summary ───────────────────────────────────────
                val analysis = remember(det) { det.toBillAnalysisResult() }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(s.billItems, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        analysis.items.forEach { item ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(item.name, fontSize = 13.sp)
                                if (item.amount > 0) Text(item.amount.toString(), fontSize = 13.sp)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(s.billTotal, fontWeight = FontWeight.SemiBold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(s.billGreenSpend, fontSize = 14.sp, color = green800)
                            Text(s.billGreenRatio(analysis.greenRatio), fontSize = 14.sp, color = green800)
                        }
                    }
                }

                // ── Environmental impact card ─────────────────────────────────
                if (det != null) {
                    EnvImpactCard(result = det.toWasteDetectResponse())
                }

                // ── Action buttons ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            capturedBytes = null
                            detectResult = null
                            uploadedImageUrl = null
                            phase = BillScanPhase.IDLE
                        }
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(s.billScanAgain)
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onScanComplete(
                                det.toBillAnalysisResult(),
                                label.ifBlank { det.items.firstOrNull()?.rawName ?: "Bill scan" },
                                uploadedImageUrl,
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = green800)
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(s.billSave)
                    }
                }

                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}
