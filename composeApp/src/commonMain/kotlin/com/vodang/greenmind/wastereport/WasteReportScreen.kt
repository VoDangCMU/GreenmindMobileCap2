package com.vodang.greenmind.wastereport

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.wastereport.CreateWasteReportRequest
import com.vodang.greenmind.api.wastereport.WasteReportDto
import com.vodang.greenmind.api.wastereport.createWasteReport
import com.vodang.greenmind.api.wastereport.getAllWasteReports
import com.vodang.greenmind.api.wastereport.getMyWasteReports
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.launch

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val orange50 = Color(0xFFFFF3E0)
private val orange   = Color(0xFFE65100)
private val red50    = Color(0xFFFFEBEE)
private val red700   = Color(0xFFC62828)
private val blue50   = Color(0xFFE3F2FD)
private val blue700  = Color(0xFF1565C0)

@Composable
fun WasteReportScreen() {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var showScan by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedReport by remember { mutableStateOf<WasteReportDto?>(null) }

    var myReports by remember { mutableStateOf<List<WasteReportDto>>(emptyList()) }
    var allReports by remember { mutableStateOf<List<WasteReportDto>>(emptyList()) }
    var isLoadingMy by remember { mutableStateOf(true) }
    var isLoadingAll by remember { mutableStateOf(false) }
    var allLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken() ?: run { isLoadingMy = false; return@LaunchedEffect }
        try {
            myReports = getMyWasteReports(token).data
        } catch (e: Throwable) {
            AppLogger.e("WasteReport", "Load my reports failed: ${e.message}")
        }
        isLoadingMy = false
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && !allLoaded) {
            isLoadingAll = true
            val token = SettingsStore.getAccessToken() ?: run { isLoadingAll = false; return@LaunchedEffect }
            try {
                allReports = getAllWasteReports(token).data
                allLoaded = true
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "Load all reports failed: ${e.message}")
            }
            isLoadingAll = false
        }
    }

    if (showScan) {
        WasteReportScanScreen(
            onReported = { form ->
                scope.launch {
                    val token = SettingsStore.getAccessToken() ?: return@launch
                    try {
                        val created = createWasteReport(
                            token,
                            CreateWasteReportRequest(
                                wasteType   = form.wasteType,
                                wardName    = form.wardName,
                                lat         = form.lat,
                                lng         = form.lng,
                                wasteKg     = form.wasteKg,
                                description = form.description,
                                imageKey    = form.imageKey,
                                imageUrl    = form.imageUrl,
                            )
                        )
                        myReports = listOf(created) + myReports
                        if (allLoaded) allReports = listOf(created) + allReports
                    } catch (e: Throwable) {
                        AppLogger.e("WasteReport", "Create failed: ${e.message}")
                    }
                }
                showScan = false
            },
            onBack = { showScan = false }
        )
        return
    }

    val myListState  = rememberLazyListState()
    val allListState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = green800,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            s.wasteReportMyTab,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            s.wasteReportAllTab,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    }
                )
            }

            val items = if (selectedTab == 0) myReports else allReports
            val isLoading = if (selectedTab == 0) isLoadingMy else isLoadingAll
            val listState = if (selectedTab == 0) myListState else allListState

            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = green800)
                }

                items.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🗑️", fontSize = 48.sp)
                        Text(
                            s.wasteReportEmpty,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items, key = { it.id }) { report ->
                        WasteReportCard(report, onClick = { selectedReport = report })
                    }
                    item { Spacer(Modifier.height(72.dp)) } // FAB clearance
                }
            }
        }

        FloatingActionButton(
            onClick = { showScan = true },
            containerColor = green800,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
            Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light)
        }
    }

    selectedReport?.let { report ->
        WasteReportDetailSheet(report = report, onDismiss = { selectedReport = null })
    }
}

@Composable
private fun WasteReportCard(report: WasteReportDto, onClick: () -> Unit) {
    val s = LocalAppStrings.current

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(orange50),
                contentAlignment = Alignment.Center,
            ) {
                Text("🗑️", fontSize = 24.sp)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        report.wasteType.replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B1B1B),
                    )
                    Text(
                        formatReportDate(report.createdAt),
                        fontSize = 11.sp,
                        color = Color.Gray,
                    )
                }
                if (report.description.isNotBlank()) {
                    Text(
                        report.description,
                        fontSize = 12.sp,
                        color = Color(0xFF616161),
                        lineHeight = 16.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "📍 ${report.wardName}",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E),
                    )
                    Text(
                        "${report.wasteKg} kg · ${report.status}",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E),
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WasteReportDetailSheet(report: WasteReportDto, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Header row: code + status chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    report.code,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B),
                )
                StatusChip(report.status)
            }

            // Report image
            if (!report.imageUrl.isNullOrBlank()) {
                NetworkImage(
                    url = report.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { previewImageUrl = report.imageUrl },
                )
            }

            // Details grid
            DetailRow("🗑️  Type", report.wasteType.replaceFirstChar { it.uppercase() })
            DetailRow("⚖️  Weight", "${report.wasteKg} kg")
            DetailRow("📍  Ward", report.wardName)
            DetailRow("🗓️  Reported", formatReportDate(report.createdAt))
            if (!report.resolvedAt.isNullOrBlank()) {
                DetailRow("✅  Resolved", formatReportDate(report.resolvedAt))
            }
            if (!report.assignedTo.isNullOrBlank()) {
                DetailRow("👷  Assigned to", report.assignedTo)
            }
            if (report.description.isNotBlank()) {
                DetailRow("📝  Note", report.description)
            }

            // Evidence image
            if (!report.imageEvidenceUrl.isNullOrBlank()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(
                    "Collection Evidence",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF616161),
                )
                NetworkImage(
                    url = report.imageEvidenceUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { previewImageUrl = report.imageEvidenceUrl },
                )
            }
        }
    }

    previewImageUrl?.let { url ->
        Dialog(
            onDismissRequest = { previewImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
                    .clickable { previewImageUrl = null },
                contentAlignment = Alignment.Center,
            ) {
                NetworkImage(
                    url = url,
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                )
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val (bg, fg) = when (status.lowercase()) {
        "pending"    -> orange50 to orange
        "assigned"   -> blue50   to blue700
        "resolved",
        "completed"  -> green50  to green600
        "rejected"   -> red50    to red700
        else         -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            status.replaceFirstChar { it.uppercase() },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            label,
            fontSize = 13.sp,
            color = Color(0xFF9E9E9E),
            modifier = Modifier.width(130.dp),
        )
        Text(
            value,
            fontSize = 13.sp,
            color = Color(0xFF1B1B1B),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Best-effort human-readable date from an ISO string. */
private fun formatReportDate(createdAt: String): String {
    // "2024-03-17T10:30:00.000Z" → "17/03 10:30"
    return try {
        val datePart = createdAt.substringBefore('T')
        val timePart = createdAt.substringAfter('T').take(5)
        val (y, m, d) = datePart.split('-')
        "$d/$m $timePart"
    } catch (_: Throwable) {
        createdAt.take(16)
    }
}
