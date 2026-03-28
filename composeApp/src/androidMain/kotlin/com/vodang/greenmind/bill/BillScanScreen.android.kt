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
import com.vodang.greenmind.api.bill.analyzeBill
import com.vodang.greenmind.i18n.LocalAppStrings
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val orange   = Color(0xFFE65100)
private val red      = Color(0xFFC62828)

private enum class BillScanPhase { IDLE, CAPTURED, ANALYZING, RESULT }

private fun billRatioColor(ratio: Int): Color = when {
    ratio >= 70 -> green800
    ratio >= 40 -> orange
    else        -> red
}

private fun formatAmount(amount: Double): String = "$${"%.2f".format(amount)}"

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
    var result by remember { mutableStateOf<BillAnalysisResult?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) {
            val uri = photoUri
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    capturedBytes = bytes
                    phase = BillScanPhase.CAPTURED
                }
            }
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                capturedBytes = bytes
                phase = BillScanPhase.CAPTURED
            }
        }
    }

    when (phase) {
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
                    Text("📷 ${s.billCapture}")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { galleryLauncher.launch("image/*") }
                ) {
                    Text("🖼 ${s.billUpload}")
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

        BillScanPhase.CAPTURED -> {
            val bytes = capturedBytes
            Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (bytes != null) {
                        val bitmap = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }
                        bitmap?.let { bmp ->
                            Image(
                                bitmap = bmp,
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
                        Text("🔄 ${s.billRetake}")
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            phase = BillScanPhase.ANALYZING
                            scope.launch {
                                try {
                                    val bytes = capturedBytes!!
                                    val token = com.vodang.greenmind.store.SettingsStore.getAccessToken()
                                    val analysisDeferred = async { analyzeBill(bytes) }
                                    val uploadDeferred = if (token != null) async {
                                        try {
                                            com.vodang.greenmind.api.upload.requestAndUpload(
                                                accessToken = token,
                                                filename = "bill_${System.currentTimeMillis()}.jpg",
                                                fileBytes = bytes,
                                                contentType = "image/jpeg",
                                            ).imageUrl
                                        } catch (_: Throwable) { null }
                                    } else null
                                    result = analysisDeferred.await()
                                    imageUrl = uploadDeferred?.await()
                                    phase = BillScanPhase.RESULT
                                } catch (e: Throwable) {
                                    error = s.billError
                                    phase = BillScanPhase.IDLE
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = green800)
                    ) {
                        Text("🔍 ${s.billAnalyze}")
                    }
                }
            }
        }

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
                }
            }
        }

        BillScanPhase.RESULT -> {
            val res = result
            if (res != null) {
                val color = billRatioColor(res.greenRatio)
                val feedback = when {
                    res.greenRatio >= 70 -> s.billRatioGood
                    res.greenRatio >= 40 -> s.billRatioOk
                    else                 -> s.billRatioLow
                }
                var storeName by remember(res) { mutableStateOf(res.storeName) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(green50)
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = storeName,
                        onValueChange = { storeName = it },
                        label = { Text(s.billStoreNameLabel) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = green800,
                            focusedLabelColor = green800,
                            cursorColor = green800
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(s.billTotal, fontSize = 11.sp, color = Color.Gray)
                                Text(formatAmount(res.totalAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
                            }
                        }
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = green50),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(s.billGreenSpend, fontSize = 11.sp, color = Color.Gray)
                                Text(formatAmount(res.greenAmount), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = green800)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(s.billItems, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
                            HorizontalDivider()
                            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                res.items.forEach { item ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            if (item.isGreen) "🌿" else "·",
                                            fontSize = 14.sp,
                                            modifier = Modifier.width(20.dp)
                                        )
                                        Text(item.name, fontSize = 13.sp, modifier = Modifier.weight(1f), color = Color(0xFF424242))
                                        Text(
                                            formatAmount(item.amount),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = if (item.isGreen) green800 else Color(0xFF757575)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(feedback, fontSize = 14.sp, color = color)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                capturedBytes = null
                                result = null
                                imageUrl = null
                                phase = BillScanPhase.IDLE
                            }
                        ) {
                            Text("🔄 ${s.billScanAgain}")
                        }
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = { onScanComplete(res, storeName, imageUrl) },
                            colors = ButtonDefaults.buttonColors(containerColor = green800)
                        ) {
                            Text("✅ ${s.billSave}")
                        }
                    }
                }
            }
        }
    }
}
