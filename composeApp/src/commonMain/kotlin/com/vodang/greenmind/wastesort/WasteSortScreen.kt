package com.vodang.greenmind.wastesort

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.api.wastesort.DetectTrashResponse
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.time.nowIso8601
import com.vodang.greenmind.wastesort.components.ScanDetailScreen
import com.vodang.greenmind.wastesort.components.WasteSortListScreen
import com.vodang.greenmind.store.HouseholdStore

// ── Palette ───────────────────────────────────────────────────────────────────

val green800 = Color(0xFF2E7D32)
val green600 = Color(0xFF388E3C)
val green50  = Color(0xFFE8F5E9)

// ── Data model ────────────────────────────────────────────────────────────────

data class WasteSortEntry(
    val id: String,
    val imageUrl: String,
    val totalObjects: Int,
    val grouped: Map<String, List<String>>,
    val createdAt: String,   // display string e.g. "Apr 2, 2026"
    val scannedBy: String,
    val pollutantResult: WasteDetectResponse? = null,
)

// ── Category helpers ──────────────────────────────────────────────────────────

fun categoryColor(cat: String) = when (cat.lowercase()) {
    "recyclable" -> Color(0xFF1565C0)
    "organic"    -> Color(0xFF2E7D32)
    "hazardous"  -> Color(0xFFD32F2F)
    else         -> Color(0xFF616161)
}

fun categoryBg(cat: String) = when (cat.lowercase()) {
    "recyclable" -> Color(0xFFE3F2FD)
    "organic"    -> Color(0xFFE8F5E9)
    "hazardous"  -> Color(0xFFFFEBEE)
    else         -> Color(0xFFF5F5F5)
}

fun categoryEmoji(cat: String) = when (cat.lowercase()) {
    "recyclable" -> "♻️"
    "organic"    -> "🌿"
    "hazardous"  -> "⚠️"
    else         -> "🗑️"
}

fun categoryLabel(cat: String) = cat.replaceFirstChar { it.uppercase() }


fun DetectTrashResponse.toEntry(scannedBy: String): WasteSortEntry {
    val date = nowIso8601().take(10) // "YYYY-MM-DD"
    return WasteSortEntry(
        id           = imageUrl.substringAfterLast("/").substringBeforeLast("."),
        imageUrl     = imageUrl,
        totalObjects = totalObjects,
        grouped      = grouped,
        createdAt    = date,
        scannedBy    = scannedBy,
    )
}

// ── Screen root ───────────────────────────────────────────────────────────────

@Composable
fun WasteSortScreen(onScanClick: () -> Unit = {}) {
    val entries by WasteSortStore.entries.collectAsState()
    var selectedEntry by remember { mutableStateOf<WasteSortEntry?>(null) }
    var showScan      by remember { mutableStateOf(false) }
    var useGallery    by remember { mutableStateOf(false) }

    val user by SettingsStore.user.collectAsState()

    LaunchedEffect(Unit) { HouseholdStore.fetchHousehold() }

    BackHandler(enabled = selectedEntry != null) { selectedEntry = null }
    BackHandler(enabled = showScan) { showScan = false }

    when {
        showScan -> {
            WasteSortScanScreen(
                onResult = { response ->
                    val entry = response.toEntry(user?.fullName ?: user?.username ?: "Me")
                    WasteSortStore.add(entry)
                    selectedEntry = entry
                    showScan = false
                },
                onBack = { showScan = false },
                useGallery = useGallery,
            )
        }
        selectedEntry != null -> {
            ScanDetailScreen(
                entry = selectedEntry!!,
                onBack = { selectedEntry = null },
            )
        }
        else -> {
            WasteSortListScreen(
                entries = entries,
                onCameraClick = { useGallery = false; showScan = true },
                onGalleryClick = { useGallery = true; showScan = true },
                onCardClick = { selectedEntry = it },
            )
        }
    }
}
