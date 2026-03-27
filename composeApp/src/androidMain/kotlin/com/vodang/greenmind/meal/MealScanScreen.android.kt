package com.vodang.greenmind.meal

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.vodang.greenmind.api.meal.MealAnalysisResult
import com.vodang.greenmind.api.meal.analyzeMeal
import com.vodang.greenmind.i18n.LocalAppStrings
import kotlinx.coroutines.launch
import java.io.File

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val orange   = Color(0xFFE65100)
private val red      = Color(0xFFC62828)

private enum class MealScanPhase { IDLE, CAPTURED, ANALYZING, RESULT }

private fun mealRatioColor(ratio: Int): Color = when {
    ratio >= 70 -> green800
    ratio >= 40 -> orange
    else        -> red
}

private fun createMealPhotoUri(context: Context): Uri {
    val dir = File(context.cacheDir, "camera_photos").also { it.mkdirs() }
    val file = File(dir, "meal_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

@Composable
actual fun MealScanScreen(
    onScanComplete: (plantRatio: Int, description: String) -> Unit,
    onBack: () -> Unit,
) {
    val s = LocalAppStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var phase by remember { mutableStateOf(MealScanPhase.IDLE) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    var capturedBytes by remember { mutableStateOf<ByteArray?>(null) }
    var result by remember { mutableStateOf<MealAnalysisResult?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) {
            val uri = photoUri
            if (uri != null) {
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null) {
                    capturedBytes = bytes
                    phase = MealScanPhase.CAPTURED
                }
            }
        }
    }

    when (phase) {
        MealScanPhase.IDLE -> {
            Column(
                modifier = Modifier.fillMaxSize().background(green50),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(green800, green600)))
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Text("←", color = Color.White, fontSize = 24.sp)
                    }
                    Text(
                        "🥦 ${s.mealScanTitle}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                Spacer(Modifier.weight(1f))
                error?.let { msg ->
                    Text(msg, color = red, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(16.dp))
                }
                Button(
                    onClick = {
                        error = null
                        val uri = createMealPhotoUri(context)
                        photoUri = uri
                        cameraLauncher.launch(uri)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = green800),
                    shape = CircleShape,
                    modifier = Modifier.size(120.dp)
                ) {
                    Text("📷", fontSize = 40.sp)
                }
                Spacer(Modifier.height(16.dp))
                Text(s.mealCapture, color = Color.Gray, fontSize = 15.sp)
                Spacer(Modifier.weight(1f))
            }
        }

        MealScanPhase.CAPTURED -> {
            val bytes = capturedBytes
            Box(modifier = Modifier.fillMaxSize()) {
                if (bytes != null) {
                    val bitmap = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap() }
                    bitmap?.let { bmp ->
                        Image(
                            bitmap = bmp,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                ) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                capturedBytes = null
                                val uri = createMealPhotoUri(context)
                                photoUri = uri
                                cameraLauncher.launch(uri)
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                        ) {
                            Text("🔄 ${s.mealRetake}")
                        }
                        Button(
                            onClick = {
                                phase = MealScanPhase.ANALYZING
                                scope.launch {
                                    try {
                                        result = analyzeMeal(capturedBytes!!)
                                        phase = MealScanPhase.RESULT
                                    } catch (e: Throwable) {
                                        error = s.mealError
                                        phase = MealScanPhase.IDLE
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = green800)
                        ) {
                            Text("🔍 ${s.mealAnalyze}")
                        }
                    }
                }
            }
        }

        MealScanPhase.ANALYZING -> {
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
                    Text(s.mealAnalyzing, color = Color.White, fontSize = 15.sp)
                }
            }
        }

        MealScanPhase.RESULT -> {
            val res = result
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(green50)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(green800, green600)))
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    Text(
                        "🥦 ${s.mealScanTitle}",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (res != null) {
                    val color = mealRatioColor(res.plantRatio)
                    val feedback = when {
                        res.plantRatio >= 70 -> s.mealRatioGood
                        res.plantRatio >= 40 -> s.mealRatioOk
                        else                 -> s.mealRatioLow
                    }
                    var mealName by remember(res) { mutableStateOf(res.description) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(color.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${res.plantRatio}%",
                                color = color,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        LinearProgressIndicator(
                            progress = { res.plantRatio / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = color.copy(alpha = 0.2f)
                        )
                        Text(s.mealPlantRatio(res.plantRatio), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = color)
                        OutlinedTextField(
                            value = mealName,
                            onValueChange = { mealName = it },
                            label = { Text(s.mealNameLabel) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = green800,
                                focusedLabelColor = green800,
                                cursorColor = green800
                            )
                        )
                        Text(feedback, fontSize = 14.sp, color = color)
                        Spacer(Modifier.weight(1f))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = {
                                capturedBytes = null
                                result = null
                                phase = MealScanPhase.IDLE
                            }) {
                                Text("🔄 ${s.mealScanAgain}")
                            }
                            Button(
                                onClick = { onScanComplete(res.plantRatio, mealName) },
                                colors = ButtonDefaults.buttonColors(containerColor = green800)
                            ) {
                                Text("✅ ${s.mealSave}")
                            }
                        }
                    }
                }
            }
        }
    }
}
