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
import com.vodang.greenmind.api.bill.toBillAnalysisResult
import com.vodang.greenmind.api.ocr.OcrResponse
import com.vodang.greenmind.api.ocr.ocrBill
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.api.upload.requestAndUpload
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.File

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val orange   = Color(0xFFE65100)
private val red      = Color(0xFFC62828)
private val gray50   = Color(0xFFF5F5F5)

private enum class BillScanPhase { IDLE, CAPTURED, ANALYZING, RESULT }

private fun billRatioColor(ratio: Int): Color = when {
    ratio >= 70 -> green800
    ratio >= 40 -> orange
    else        -> red
}

private fun currencySymbol(currency: String?): String = when (currency?.uppercase()) {
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    "VND" -> "₫"
    else  -> currency?.let { "$it " } ?: "$"
}

private fun formatAmount(amount: Double, symbol: String = "$"): String =
    "$symbol${"%.2f".format(amount)}"

private fun createBillPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "bill_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
actual fun BillScanScreen(
    onScanComplete: (result: BillAnalysisResult, storeName: String, imageUrl: String?) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phase by remember { mutableStateOf(BillScanPhase.IDLE) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var ocrResult by remember { mutableStateOf<OcrResponse?>(null) }
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
                ) { Text("📷 ${s.billCapture}") }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { galleryLauncher.launch("image/*") }
                ) { Text("🖼 ${s.billUpload}") }
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
                    ) { Text("🔄 ${s.billRetake}") }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            phase = BillScanPhase.ANALYZING
                            scope.launch {
                                try {
                                    val token = SettingsStore.getAccessToken()
                                        ?: throw IllegalStateException("Not logged in")
                                    val b = capturedBytes!!
                                    val filename = "bill_${System.currentTimeMillis()}.jpg"
                                    // Fire OCR and upload in parallel
                                    coroutineScope {
                                        val ocrDeferred = async {
                                            ocrBill(accessToken = token, imageBytes = b, filename = filename)
                                        }
                                        val uploadDeferred = async {
                                            try {
                                                requestAndUpload(
                                                    accessToken = token,
                                                    filename    = filename,
                                                    fileBytes   = b,
                                                    contentType = "image/jpeg",
                                                ).imageUrl
                                            } catch (_: Throwable) { null }
                                        }
                                        ocrResult = ocrDeferred.await()
                                        uploadedImageUrl = uploadDeferred.await()
                                    }
                                    phase = BillScanPhase.RESULT
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

        // ── Step 3: Waiting for OCR ───────────────────────────────────────────
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
                    // OCR can take 30-60 s on cold start — reassure the user
                    Text("This may take up to a minute…", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        }

        // ── Step 4: Show OCR result ───────────────────────────────────────────
        BillScanPhase.RESULT -> {
            val ocr = ocrResult ?: run { phase = BillScanPhase.IDLE; return }
            val mapped = remember(ocr) { ocr.toBillAnalysisResult() }
            val symbol = remember(ocr) { currencySymbol(ocr.doc?.currency) }
            val ratioColor = billRatioColor(mapped.greenRatio)
            val feedback = when {
                mapped.greenRatio >= 70 -> s.billRatioGood
                mapped.greenRatio >= 40 -> s.billRatioOk
                else                    -> s.billRatioLow
            }

            var storeName by remember(ocr) { mutableStateOf(ocr.vendor?.name ?: "") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gray50)
                    .statusBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Vendor / header card ──────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = storeName,
                            onValueChange = { storeName = it },
                            label = { Text(s.billStoreNameLabel) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = green800,
                                focusedLabelColor  = green800,
                                cursorColor        = green800,
                            )
                        )
                        if (!ocr.vendor?.address.isNullOrBlank()) {
                            Text(
                                "📍 ${ocr.vendor!!.address}",
                                fontSize = 12.sp,
                                color = Color(0xFF757575),
                                lineHeight = 17.sp,
                            )
                        }
                        val dateTime = listOfNotNull(ocr.datetime?.date, ocr.datetime?.time)
                            .joinToString("  ·  ")
                        if (dateTime.isNotBlank()) {
                            Text("🗓 $dateTime", fontSize = 12.sp, color = Color(0xFF757575))
                        }
                        if (!ocr.doc?.paymentMethod.isNullOrBlank()) {
                            Text("💳 ${ocr.doc!!.paymentMethod}", fontSize = 12.sp, color = Color(0xFF757575))
                        }
                    }
                }

                // ── Items card ────────────────────────────────────────────────
                if (!ocr.items.isNullOrEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                s.billItems,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242),
                            )
                            Spacer(Modifier.height(8.dp))
                            // Column header
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text("Item", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                                Text("Qty", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(28.dp))
                                Text("Unit", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(52.dp))
                                Text("Total", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(54.dp))
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                ocr.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        // Name + brand + plant badge
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            ) {
                                                Text(
                                                    if (item.plantBased == true) "🌿" else "·",
                                                    fontSize = 12.sp,
                                                )
                                                Text(
                                                    item.rawName ?: "Unknown",
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF212121),
                                                    fontWeight = FontWeight.Medium,
                                                )
                                            }
                                            if (!item.brand.isNullOrBlank()) {
                                                Text(
                                                    item.brand,
                                                    fontSize = 11.sp,
                                                    color = Color.Gray,
                                                    modifier = Modifier.padding(start = 20.dp),
                                                )
                                            }
                                        }
                                        Text(
                                            "${item.quantity ?: "—"}",
                                            fontSize = 12.sp,
                                            color = Color(0xFF616161),
                                            modifier = Modifier.width(28.dp),
                                        )
                                        Text(
                                            item.unitPrice?.let { formatAmount(it, symbol) } ?: "—",
                                            fontSize = 12.sp,
                                            color = Color(0xFF616161),
                                            modifier = Modifier.width(52.dp),
                                        )
                                        Text(
                                            item.lineTotal?.let { formatAmount(it, symbol) } ?: "—",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (item.plantBased == true) green600 else Color(0xFF424242),
                                            modifier = Modifier.width(54.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // ── Totals card ───────────────────────────────────────────────
                ocr.totals?.let { t ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            t.subtotal?.let  { TotalsRow("Subtotal",  formatAmount(it, symbol)) }
                            t.discount?.let  { if (it != 0.0) TotalsRow("Discount", "- ${formatAmount(it, symbol)}", red) }
                            t.tax?.let       { if (it != 0.0) TotalsRow("Tax", formatAmount(it, symbol)) }
                            if (t.grandTotal != null) {
                                HorizontalDivider()
                                TotalsRow(
                                    label = "Grand Total",
                                    value = formatAmount(t.grandTotal, symbol),
                                    valueColor = green800,
                                    bold = true,
                                )
                            }
                        }
                    }
                }

                // ── Green summary row ─────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryChip(
                        label = s.billGreenSpend,
                        value = formatAmount(mapped.greenAmount, symbol),
                        bg = green50,
                        fg = green800,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryChip(
                        label = "Green ratio",
                        value = "${mapped.greenRatio}%",
                        bg = ratioColor.copy(alpha = 0.1f),
                        fg = ratioColor,
                        modifier = Modifier.weight(1f),
                    )
                }

                Text(feedback, fontSize = 13.sp, color = ratioColor)

                // ── Action buttons ────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            capturedBytes = null
                            ocrResult = null
                            uploadedImageUrl = null
                            phase = BillScanPhase.IDLE
                        }
                    ) { Text("🔄 ${s.billScanAgain}") }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { onScanComplete(mapped, storeName, uploadedImageUrl) },
                        colors = ButtonDefaults.buttonColors(containerColor = green800)
                    ) { Text("✅ ${s.billSave}") }
                }

                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun TotalsRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF424242),
    bold: Boolean = false,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF757575))
        Text(
            value,
            fontSize = if (bold) 15.sp else 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    value: String,
    bg: Color,
    fg: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = fg)
        }
    }
}
