package com.vodang.greenmind.householdwaste

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
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
import com.vodang.greenmind.householdwaste.components.*
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.wastereport.WasteReportCard
import com.vodang.greenmind.wastereport.WasteReportDetailSheet
import com.vodang.greenmind.platform.BackHandler

private val Green700 = Color(0xFF2E7D32)
private val Green500 = Color(0xFF43A047)
private val Green50  = Color(0xFFE8F5E9)
private val Red600   = Color(0xFFDC2626)
private val Red50    = Color(0xFFFEE2E2)
private val Blue600  = Color(0xFF2563EB)
private val Blue50   = Color(0xFFDBEAFE)
private val Surface  = Color(0xFFFFFBFE)
private val SurfaceVariant = Color(0xFFE7E0EC)
private val OnSurfaceVariant = Color(0xFF49454F)
private val Outline = Color(0xFF79747E)

@Composable
fun HouseholdWasteScreen(
    onBack: () -> Unit = {},
    onNavigateToWasteImpact: () -> Unit = {},
    scrollState: ScrollState? = null,
    onSettingsClick: () -> Unit = {}
) {
    val s = LocalAppStrings.current
    val household by HouseholdStore.household.collectAsState()
    val hasFetched by HouseholdStore.hasFetched.collectAsState()
    val isFetching by HouseholdStore.isFetching.collectAsState()

    var householdHistory   by remember { mutableStateOf<List<DetectTrashHistoryDto>>(emptyList()) }
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
            CircularProgressIndicator(color = Green700)
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
        return
    }

    val localScrollState = scrollState ?: rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
            .verticalScroll(localScrollState)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                color = Green50
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Filled.Eco, contentDescription = null, tint = Green700, modifier = Modifier.size(20.dp))
                    Column {
                        Text(s.scoreLabel(currentScore), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Green700)
                        Text(s.basedOnScans(greenScoreEntries.size), fontSize = 11.sp, color = Green500)
                    }
                }
            }
            Surface(shape = RoundedCornerShape(12.dp), color = Green50) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Group, contentDescription = null, tint = Green700, modifier = Modifier.size(18.dp))
                    Text(s.memberCount(h.members?.size ?: 0), fontSize = 12.sp, color = Green700)
                }
            }
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, contentDescription = s.householdSettings, tint = Green700)
            }
        }

        if (isLoadingData) {
            Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Green700)
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Filled.Insights,
                    title = s.viewWasteImpactAnalysis,
                    subtitle = s.environmentalImpactShort,
                    containerColor = Green50,
                    contentColor = Green700,
                    onClick = onNavigateToWasteImpact,
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    icon = Icons.Filled.Analytics,
                    title = s.wasteStatTitle,
                    subtitle = s.wasteStatShort,
                    containerColor = Blue50,
                    contentColor = Blue600,
                    onClick = { },
                    modifier = Modifier.weight(1f)
                )
            }

            HorizontalDivider(color = Outline.copy(alpha = 0.3f))

            val householdGroups = householdHistory.groupBy { it.imageUrl }
            SectionCard(
                title = s.greenScoreSection,
                icon = Icons.Filled.Eco,
                iconColor = Green700,
                count = greenScoreEntries.size,
                onViewAll = if (greenScoreEntries.isNotEmpty()) {{ viewAllMode = ViewAllMode.GREEN_SCORES }} else null
            ) {
                if (greenScoreEntries.isEmpty()) {
                    EmptyStateMessage(icon = Icons.Outlined.Eco, message = s.noScoreHistory)
                } else {
                    GreenScoreCard(entry = greenScoreEntries.last(), onClick = { selectedScore = greenScoreEntries.last() })
                    if (greenScoreEntries.size > 1) {
                        TextButton(onClick = { viewAllMode = ViewAllMode.GREEN_SCORES }, modifier = Modifier.fillMaxWidth()) {
                            Text(s.viewAllHistory, color = Green700)
                        }
                    }
                }
            }

            SectionCard(
                title = s.householdScansSection,
                icon = Icons.Filled.CameraAlt,
                iconColor = Green700,
                count = householdGroups.size,
                onViewAll = if (householdGroups.isNotEmpty()) {{ viewAllMode = ViewAllMode.HOUSEHOLD_SCANS }} else null
            ) {
                if (householdGroups.isEmpty()) {
                    EmptyStateMessage(icon = Icons.Outlined.CameraAlt, message = s.noHouseholdScans)
                } else {
                    householdGroups.values.take(2).forEach { records ->
                        GroupedDetectScanCard(records = records, onClick = { selectedScanGroup = records })
                        Spacer(Modifier.height(8.dp))
                    }
                    if (householdGroups.size > 2) {
                        TextButton(onClick = { viewAllMode = ViewAllMode.HOUSEHOLD_SCANS }, modifier = Modifier.fillMaxWidth()) {
                            Text(s.viewAllItems, color = Green700)
                        }
                    }
                }
            }

            val userGroups = userHistory.groupBy { it.imageUrl }
            SectionCard(
                title = s.myScanReports,
                icon = Icons.Filled.Person,
                iconColor = Blue600,
                count = userGroups.size,
                onViewAll = if (userGroups.isNotEmpty()) {{ viewAllMode = ViewAllMode.USER_SCANS }} else null
            ) {
                if (userGroups.isEmpty()) {
                    EmptyStateMessage(icon = Icons.Outlined.Person, message = s.myScanReports)
                } else {
                    userGroups.values.take(2).forEach { records ->
                        GroupedDetectScanCard(records = records, onClick = { selectedScanGroup = records })
                        Spacer(Modifier.height(8.dp))
                    }
                    if (userGroups.size > 2) {
                        TextButton(onClick = { viewAllMode = ViewAllMode.USER_SCANS }, modifier = Modifier.fillMaxWidth()) {
                            Text(s.viewAllItems, color = Green700)
                        }
                    }
                }
            }

            SectionCard(
                title = s.myWasteReports,
                icon = Icons.Filled.Warning,
                iconColor = Red600,
                count = wasteReports.size,
                onViewAll = if (wasteReports.isNotEmpty()) {{ viewAllMode = ViewAllMode.WASTE_REPORTS }} else null
            ) {
                if (wasteReports.isEmpty()) {
                    EmptyStateMessage(icon = Icons.Outlined.Warning, message = s.noWasteReports)
                } else {
                    wasteReports.take(2).forEach { report ->
                        WasteReportCard(report = report, onClick = { selectedReport = report })
                        Spacer(Modifier.height(8.dp))
                    }
                    if (wasteReports.size > 2) {
                        TextButton(onClick = { viewAllMode = ViewAllMode.WASTE_REPORTS }, modifier = Modifier.fillMaxWidth()) {
                            Text(s.viewAllItems, color = Green700)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

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

@Composable
private fun SectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    count: Int,
    onViewAll: (() -> Unit)?,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = iconColor)
                    Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (count > 0) {
                        SuggestionChip(
                            onClick = { },
                            label = { Text("$count") },
                            colors = SuggestionChipDefaults.suggestionChipColors(containerColor = iconColor.copy(alpha = 0.1f), labelColor = iconColor),
                            border = null
                        )
                    }
                    if (onViewAll != null) {
                        IconButton(onClick = onViewAll, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            content()
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = contentColor)
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = contentColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitle, fontSize = 10.sp, color = contentColor.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun GreenScoreCard(entry: GreenScoreEntryDto, onClick: () -> Unit) {
    val s = LocalAppStrings.current
    val isPositive = entry.delta >= 0
    val deltaColor = if (isPositive) Green700 else Red600
    val deltaBg = if (isPositive) Green50 else Red50
    val deltaText = if (isPositive) "+${entry.delta}" else "${entry.delta}"

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Green50)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(s.currentScore, fontSize = 12.sp, color = OnSurfaceVariant)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("${entry.finalScore}", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = Green700)
                    Box(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(deltaBg).padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(deltaText, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = deltaColor)
                    }
                }
            }
            Icon(Icons.Filled.EmojiEvents, contentDescription = null, modifier = Modifier.size(40.dp), tint = Green700.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun EmptyStateMessage(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceVariant.copy(alpha = 0.5f)).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp), tint = OnSurfaceVariant.copy(alpha = 0.6f))
        Text(message, fontSize = 14.sp, color = OnSurfaceVariant.copy(alpha = 0.7f))
    }
}

@Composable
internal fun RemainingCard(remaining: Int, label: String, onClick: () -> Unit) {
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(SurfaceVariant.copy(alpha = 0.5f)).clickable(onClick = onClick).padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("+ $remaining more $label${if (remaining != 1) "s" else ""}  ·  View all", fontSize = 12.sp, color = OnSurfaceVariant.copy(alpha = 0.7f), fontWeight = FontWeight.Medium)
    }
}
