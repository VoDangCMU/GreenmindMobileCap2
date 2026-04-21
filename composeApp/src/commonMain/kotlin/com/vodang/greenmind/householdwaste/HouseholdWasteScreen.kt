package com.vodang.greenmind.householdwaste

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.GreenScoreEntryDto
import com.vodang.greenmind.api.households.getDetectByHouseholdId
import com.vodang.greenmind.api.households.getDetectHistoryByUser
import com.vodang.greenmind.api.households.getGreenScoreByHousehold
import com.vodang.greenmind.api.wastereport.WasteReportDto
import com.vodang.greenmind.api.wastereport.getMyWasteReports
import com.vodang.greenmind.householdwaste.components.CreateHouseholdScreen
import com.vodang.greenmind.householdwaste.components.GreenScoreDetailSheet
import com.vodang.greenmind.householdwaste.components.GreenScoreSection
import com.vodang.greenmind.householdwaste.components.GroupedDetectScanDetailSheet
import com.vodang.greenmind.householdwaste.components.GroupedDetectScanCard
import com.vodang.greenmind.householdwaste.components.EmptyState
import com.vodang.greenmind.householdwaste.components.SectionHeader
import com.vodang.greenmind.householdwaste.components.ViewAllMode
import com.vodang.greenmind.householdwaste.components.ViewAllRecordsScreen
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.wastereport.WasteReportCard
import com.vodang.greenmind.wastereport.WasteReportDetailSheet
import com.vodang.greenmind.platform.BackHandler

// ── Palette ───────────────────────────────────────────────────────────────────

private val bgGray   = Color(0xFFF3F4F6)
private val green700 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)
private val red600   = Color(0xFFDC2626)
private val red50    = Color(0xFFFEF2F2)
private val gray700  = Color(0xFF374151)
private val gray400  = Color(0xFF9CA3AF)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun HouseholdWasteScreen(onBack: () -> Unit = {}, onNavigateToWasteImpact: () -> Unit = {}) {
    val s = LocalAppStrings.current
    var showSettings by remember { mutableStateOf(false) }

    if (showSettings) {
        HouseholdSettingsScreen(onBack = { showSettings = false })
        return
    }

    val household by HouseholdStore.household.collectAsState()
    val hasFetched by HouseholdStore.hasFetched.collectAsState()
    val isFetching by HouseholdStore.isFetching.collectAsState()

    var householdHistory  by remember { mutableStateOf<List<DetectTrashHistoryDto>>(emptyList()) }
    var userHistory       by remember { mutableStateOf<List<DetectTrashHistoryDto>>(emptyList()) }
    var greenScoreEntries by remember { mutableStateOf<List<GreenScoreEntryDto>>(emptyList()) }
    var wasteReports      by remember { mutableStateOf<List<WasteReportDto>>(emptyList()) }
    var selectedReport    by remember { mutableStateOf<WasteReportDto?>(null) }
    var selectedScanGroup by remember { mutableStateOf<List<DetectTrashHistoryDto>?>(null) }
    var selectedScore     by remember { mutableStateOf<GreenScoreEntryDto?>(null) }
    var isLoadingData     by remember { mutableStateOf(false) }
    var viewAllMode       by remember { mutableStateOf(ViewAllMode.NONE) }
    val refreshTrigger    by com.vodang.greenmind.store.WasteSortStore.refreshTrigger.collectAsState()

    LaunchedEffect(Unit) { HouseholdStore.fetchHousehold() }

    LaunchedEffect(household, refreshTrigger) {
        val h = household ?: return@LaunchedEffect
        val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect
        isLoadingData = true
        try {
            val scanResp    = runCatching { getDetectByHouseholdId(token, h.id) }
            val userResp    = runCatching { getDetectHistoryByUser(token) }
            val scoreResp   = runCatching { getGreenScoreByHousehold(token, h.id) }
            val reportsResp = runCatching { getMyWasteReports(token) }
            scanResp.getOrNull()?.data?.let    { householdHistory  = it }
            userResp.getOrNull()?.data?.let    { userHistory       = it }
            scoreResp.getOrNull()?.data?.greenScores?.let { greenScoreEntries = it }
            reportsResp.getOrNull()?.data?.let { wasteReports      = it }
        } finally {
            isLoadingData = false
        }
    }

    if (!hasFetched || isFetching) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    if (household == null) {
        CreateHouseholdScreen(onBack = onBack, onSuccess = { HouseholdStore.fetchHousehold() })
        return
    }

    val h = household!!
    val currentScore = greenScoreEntries.lastOrNull()?.finalScore ?: h.scoreGreen

    if (viewAllMode != ViewAllMode.NONE) {
        BackHandler { viewAllMode = ViewAllMode.NONE }

        val householdGroupsList = remember(householdHistory) { householdHistory.groupBy { it.imageUrl }.values.toList() }
        val userGroupsList = remember(userHistory) { userHistory.groupBy { it.imageUrl }.values.toList() }
        ViewAllRecordsScreen(
            mode = viewAllMode,
            householdGroups = householdGroupsList,
            userGroups = userGroupsList,
            wasteReports = wasteReports,
            greenScores = greenScoreEntries,
            onBack = { viewAllMode = ViewAllMode.NONE },
            onReportClick = { selectedReport = it },
            onScanGroupClick = { selectedScanGroup = it },
            onScoreClick = { selectedScore = it }
        )
        selectedReport?.let { report -> WasteReportDetailSheet(report = report, onDismiss = { selectedReport = null }) }
        selectedScanGroup?.let { group -> GroupedDetectScanDetailSheet(records = group, onDismiss = { selectedScanGroup = null }) }
        selectedScore?.let { score -> GreenScoreDetailSheet(entry = score, onDismiss = { selectedScore = null }) }
        return
    }

    Box(Modifier.fillMaxSize().background(bgGray)) {
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // ── Header ────────────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(green700)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(s.householdDashboard, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(h.address, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(s.memberCount(h.members?.size ?: 0), fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                }
            }

            if (isLoadingData) {
                Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = green700)
                }
            } else {
                Column(
                    Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // ── Section 1: Green Score ────────────────────────────────
                    SectionHeader("Green Score", greenScoreEntries.size, Icons.Filled.Eco)
                    if (greenScoreEntries.isEmpty()) {
                        EmptyState(s.noScoreHistory)
                    } else {
                        GreenScoreSection(greenScoreEntries, onOpenDetail = { selectedScore = it }, onViewAll = { viewAllMode = ViewAllMode.GREEN_SCORES })
                    }

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(green50)
                            .clickable { onNavigateToWasteImpact() }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.viewWasteImpactAnalysis, fontSize = 13.sp, color = green700, fontWeight = FontWeight.Medium)
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    // ── Section 2: Scan History ───────────────────────────────
                    val householdGroups = householdHistory.groupBy { it.imageUrl }
                    SectionHeader("Scan History", householdGroups.size, Icons.Filled.CameraAlt)
                    if (householdGroups.isEmpty()) {
                        EmptyState(s.noHouseholdScans)
                    } else {
                        householdGroups.values.take(3).forEach { records ->
                            GroupedDetectScanCard(records, onClick = { selectedScanGroup = records })
                        }
                        val rem = householdGroups.size - 3
                        if (rem > 0) RemainingCard(rem, "scan", onClick = { viewAllMode = ViewAllMode.HOUSEHOLD_SCANS })
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    // ── Section 3: My Scan Reports ────────────────────────────
                    val userGroups = userHistory.groupBy { it.imageUrl }
                    SectionHeader(s.myScanReports, userGroups.size)
                    if (userGroups.isEmpty()) {
                        EmptyState(s.myScanReports)
                    } else {
                        userGroups.values.take(3).forEach { records ->
                            GroupedDetectScanCard(records, onClick = { selectedScanGroup = records })
                        }
                        val rem = userGroups.size - 3
                        if (rem > 0) RemainingCard(rem, "scan", onClick = { viewAllMode = ViewAllMode.USER_SCANS })
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    // ── Section 4: My Waste Reports ───────────────────────────
                    SectionHeader(s.myWasteReports, wasteReports.size)
                    if (wasteReports.isEmpty()) {
                        EmptyState(s.noWasteReports)
                    } else {
                        wasteReports.take(3).forEach { report ->
                            WasteReportCard(report = report, onClick = { selectedReport = report })
                        }
                        val remaining = wasteReports.size - 3
                        if (remaining > 0) {
                            RemainingCard(remaining, "report", onClick = { viewAllMode = ViewAllMode.WASTE_REPORTS })
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable { showSettings = true }
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(s.householdSettings, fontSize = 14.sp, color = green700, fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    }

    // Detail sheets — rendered outside the scroll container so they overlay correctly
    selectedReport?.let { report ->
        WasteReportDetailSheet(report = report, onDismiss = { selectedReport = null })
    }
    selectedScanGroup?.let { group ->
        GroupedDetectScanDetailSheet(records = group, onDismiss = { selectedScanGroup = null })
    }
    selectedScore?.let { score ->
        GreenScoreDetailSheet(entry = score, onDismiss = { selectedScore = null })
    }
}

// ── Remaining card ────────────────────────────────────────────────────────────

@Composable
internal fun RemainingCard(remaining: Int, label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F4F0))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            "+ $remaining more $label${if (remaining != 1) "s" else ""}  ·  View all",
            fontSize = 12.sp,
            color = gray400,
            fontWeight = FontWeight.Medium
        )
    }
}