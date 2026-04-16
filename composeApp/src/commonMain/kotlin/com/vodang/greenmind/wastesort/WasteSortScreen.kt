package com.vodang.greenmind.wastesort

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.getDetectHistoryByUser
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.api.wastesort.DetectTrashResponse
import com.vodang.greenmind.householdwaste.components.GroupedDetectScanDetailSheet
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.time.nowIso8601
import com.vodang.greenmind.wastesort.components.ScanDetailScreen
import com.vodang.greenmind.wastesort.components.WasteSortListScreen
import com.vodang.greenmind.util.AppLogger
import com.vodang.greenmind.i18n.AppStrings

// ── Palette ───────────────────────────────────────────────────────────────────

val green800 = Color(0xFF2E7D32)
val green600 = Color(0xFF388E3C)
val green50  = Color(0xFFE8F5E9)

// ── Data model ────────────────────────────────────────────────────────────────

/** Lifecycle stages for a scanned entry. */
enum class WasteSortStatus { SCANNED, SORTED, BRINGOUTED, COLLECTED }

val WasteSortStatus.label: String
    get() = when (this) {
        WasteSortStatus.SCANNED    -> "Scanned"
        WasteSortStatus.SORTED     -> "Sorted"
        WasteSortStatus.BRINGOUTED -> "Brought Out"
        WasteSortStatus.COLLECTED  -> "Collected"
    }

fun WasteSortStatus.i18nLabel(s: AppStrings) = when (this) {
    WasteSortStatus.SCANNED    -> s.wasteScanned
    WasteSortStatus.SORTED     -> s.wasteSorted
    WasteSortStatus.BRINGOUTED -> s.wasteBroughtOut
    WasteSortStatus.COLLECTED  -> s.wasteCollected
}

data class WasteSortEntry(
    val id: String,
    /** Backend DB record ID — used for API calls like bring-out. Null for legacy/offline entries. */
    val backendId: String? = null,
    val imageUrl: String,
    val totalObjects: Int,
    val grouped: Map<String, List<String>>,
    val createdAt: String,   // display string e.g. "Apr 2, 2026"
    val scannedBy: String,
    val status: WasteSortStatus = WasteSortStatus.SCANNED,
    val pollutantResult: WasteDetectResponse? = null,
)

// ── Category helpers ──────────────────────────────────────────────────────────

fun categoryColor(cat: String) = when (cat.lowercase()) {
    "recyclable" -> Color(0xFF1565C0)
    "residual"   -> Color(0xFF6D4C41)
    "organic"    -> Color(0xFF2E7D32)
    "hazardous"  -> Color(0xFFD32F2F)
    else         -> Color(0xFF616161)
}

fun categoryBg(cat: String) = when (cat.lowercase()) {
    "recyclable" -> Color(0xFFE3F2FD)
    "residual"   -> Color(0xFFEFEBE9)
    "organic"    -> Color(0xFFE8F5E9)
    "hazardous"  -> Color(0xFFFFEBEE)
    else         -> Color(0xFFF5F5F5)
}

fun categoryEmoji(cat: String) = when (cat.lowercase()) {
    "recyclable" -> "♻️"
    "residual"   -> "🗑️"
    "organic"    -> "🌿"
    "hazardous"  -> "⚠️"
    else         -> "📦"
}

fun categoryLabel(cat: String) = cat.replaceFirstChar { it.uppercase() }


fun DetectTrashResponse.toEntry(scannedBy: String): WasteSortEntry {
    val date = nowIso8601().take(10) // "YYYY-MM-DD"
    return WasteSortEntry(
        id           = imageUrl.substringAfterLast("/").substringBeforeLast("."),
        backendId    = backendId,
        imageUrl     = imageUrl,
        totalObjects = totalObjects,
        grouped      = grouped,
        createdAt    = date,
        scannedBy    = scannedBy,
        status       = WasteSortStatus.SCANNED,
    )
}

// ── Screen root ───────────────────────────────────────────────────────────────

@Composable
fun WasteSortScreen(onScanClick: () -> Unit = {}) {
    val entries by WasteSortStore.entries.collectAsState()
    var selectedEntry   by remember { mutableStateOf<WasteSortEntry?>(null) }
    var selectedApiGroup by remember { mutableStateOf<List<DetectTrashHistoryDto>?>(null) }
    var showScan        by remember { mutableStateOf(false) }
    var useGallery      by remember { mutableStateOf(false) }
    var apiHistory      by remember { mutableStateOf<List<DetectTrashHistoryDto>>(emptyList()) }
    var isLoadingHistory by remember { mutableStateOf(false) }

    val user by SettingsStore.user.collectAsState()
    val refreshTrigger by WasteSortStore.refreshTrigger.collectAsState()

    LaunchedEffect(refreshTrigger) {
        HouseholdStore.fetchHousehold()
        val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect
        isLoadingHistory = true
        try {
            apiHistory = getDetectHistoryByUser(token).data
        } catch (e: Throwable) {
            AppLogger.e("WasteSortScreen", "Failed to load scan history: ${e.message}")
        } finally {
            isLoadingHistory = false
        }
    }

    BackHandler(enabled = selectedEntry != null) { selectedEntry = null }
    BackHandler(enabled = showScan) { showScan = false }

    when {
        showScan -> {
            WasteSortScanScreen(
                onResult = { response ->
                    val entry = response.toEntry(user?.fullName ?: user?.username ?: "Me")
                    WasteSortStore.add(entry)
                    WasteSortStore.triggerRefresh()
                    selectedEntry = entry
                    showScan = false
                },
                onBack = { showScan = false },
                useGallery = useGallery,
            )
        }
        selectedEntry != null -> {
            // Keep selectedEntry in sync with store updates (e.g. status changes)
            val liveEntry = entries.find { it.id == selectedEntry!!.id } ?: selectedEntry!!
            ScanDetailScreen(
                entry = liveEntry,
                onBack = { selectedEntry = null },
                onStatusChange = { newStatus ->
                    WasteSortStore.updateStatus(liveEntry.id, newStatus)
                    selectedEntry = liveEntry.copy(status = newStatus)
                },
            )
        }
        else -> {
            WasteSortListScreen(
                entries = entries,
                apiHistory = apiHistory,
                isLoadingHistory = isLoadingHistory,
                onCameraClick = { useGallery = false; showScan = true },
                onGalleryClick = { useGallery = true; showScan = true },
                onCardClick = { selectedEntry = it },
                onApiScanClick = { selectedApiGroup = it },
            )
        }
    }

    selectedApiGroup?.let { group ->
        GroupedDetectScanDetailSheet(records = group, onDismiss = { selectedApiGroup = null })
    }
}
