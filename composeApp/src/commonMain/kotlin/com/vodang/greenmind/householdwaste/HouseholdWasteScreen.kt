package com.vodang.greenmind.householdwaste

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
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
import com.vodang.greenmind.api.households.getCurrentUserHousehold
import com.vodang.greenmind.api.households.getDetectHistoryByHousehold
import com.vodang.greenmind.api.households.getDetectHistoryByUser
import com.vodang.greenmind.api.households.getGreenScoreByHousehold
import com.vodang.greenmind.householdwaste.components.CreateHouseholdForm
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.wastereport.NetworkImage

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
fun HouseholdWasteScreen(onBack: () -> Unit = {}) {
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
    var isLoadingData     by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { HouseholdStore.fetchHousehold() }

    LaunchedEffect(household) {
        val h = household ?: return@LaunchedEffect
        val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect
        isLoadingData = true
        try {
            val (scanResp, userResp, scoreResp) = Triple(
                runCatching { getDetectHistoryByHousehold(token) },
                runCatching { getDetectHistoryByUser(token) },
                runCatching { getGreenScoreByHousehold(token, h.id) }
            )
            scanResp.getOrNull()?.data?.let  { householdHistory  = it }
            userResp.getOrNull()?.data?.let  { userHistory       = it }
            scoreResp.getOrNull()?.data?.greenScores?.let { greenScoreEntries = it }
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
                    Text("Household Dashboard", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(h.address, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    val memberCount = h.members?.size ?: 0
                    Text("$memberCount member${if (memberCount != 1) "s" else ""}", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                }
                Row(
                    Modifier.align(Alignment.CenterEnd),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Settings button
                    IconButton(
                        onClick = { showSettings = true },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.15f)),
                    ) {
                        Text("⚙", fontSize = 18.sp, color = Color.White)
                    }
                    // Current score badge
                    Box(
                        Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$currentScore", fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                            Text("score", fontSize = 10.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }
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
                    // ── Section 1: Scan History ───────────────────────────────
                    SectionHeader("📷  Scan History", householdHistory.size)
                    if (householdHistory.isEmpty()) {
                        EmptyState("No household scans yet")
                    } else {
                        householdHistory.forEach { ScanCard(it) }
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    // ── Section 2: Green Score ────────────────────────────────
                    SectionHeader("🌿  Green Score", greenScoreEntries.size)
                    if (greenScoreEntries.isEmpty()) {
                        EmptyState("No score history yet")
                    } else {
                        GreenScoreSection(greenScoreEntries)
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    // ── Section 3: My Reports ─────────────────────────────────
                    SectionHeader("👤  My Reports", userHistory.size)
                    if (userHistory.isEmpty()) {
                        EmptyState("You haven't submitted any reports yet")
                    } else {
                        userHistory.forEach { ScanCard(it) }
                    }

                    Spacer(Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

// ── Section header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = gray700)
        Box(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE5E7EB))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text("$count", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = gray700)
        }
    }
}

// ── Empty state ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(message: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(message, fontSize = 13.sp, color = gray400)
    }
}

// ── Scan card ─────────────────────────────────────────────────────────────────

@Composable
private fun ScanCard(record: DetectTrashHistoryDto) {
    val detectLabel = when (record.detectType) {
        "detect_trash"            -> "Detect Trash"
        "predict_pollutant_impact" -> "Pollutant Impact"
        "total_mass"              -> "Total Mass"
        else                      -> record.detectType ?: "Unknown"
    }
    val labelColor = when (record.detectType) {
        "predict_pollutant_impact" -> Color(0xFFB45309)
        "total_mass"               -> Color(0xFF1D4ED8)
        else                       -> green700
    }
    val labelBg = when (record.detectType) {
        "predict_pollutant_impact" -> Color(0xFFFEF3C7)
        "total_mass"               -> Color(0xFFEFF6FF)
        else                       -> green50
    }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NetworkImage(
                url = record.imageUrl,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(10.dp))
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(labelBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(detectLabel, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = labelColor)
                    }
                    Text(
                        record.createdAt?.take(10) ?: "",
                        fontSize = 10.sp,
                        color = gray400
                    )
                }
                Text(
                    "${record.totalObjects ?: 0} objects detected",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = gray700
                )
                record.items?.take(3)?.joinToString(" · ") { "${it.quantity}× ${it.name}" }?.let {
                    Text(it, fontSize = 11.sp, color = gray400, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                record.totalMassKg?.let {
                    Text("⚖️ %.2f kg".format(it), fontSize = 11.sp, color = green700)
                }
            }
        }
    }
}

// ── Green score section ───────────────────────────────────────────────────────

@Composable
private fun GreenScoreSection(entries: List<GreenScoreEntryDto>) {
    val latest = entries.last()

    // Current score card
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Current Score", fontSize = 12.sp, color = gray400)
                Text("${latest.finalScore}", fontSize = 42.sp, fontWeight = FontWeight.ExtraBold, color = green700)
                val deltaColor = if (latest.delta >= 0) green700 else red600
                val deltaPrefix = if (latest.delta >= 0) "+" else ""
                Text("$deltaPrefix${latest.delta} from last scan", fontSize = 12.sp, color = deltaColor)
            }
            // Score ring
            Box(
                Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(50))
                    .background(if (latest.finalScore >= 50) green50 else red50),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (latest.finalScore >= 70) "🌟" else if (latest.finalScore >= 40) "🌿" else "⚠️",
                    fontSize = 32.sp
                )
            }
        }
    }

    Spacer(Modifier.height(8.dp))

    // Score history
    Text("Score History", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = gray700)
    Spacer(Modifier.height(4.dp))

    entries.reversed().forEach { entry ->
        ScoreEntryRow(entry)
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ScoreEntryRow(entry: GreenScoreEntryDto) {
    val isPositive = entry.delta >= 0
    val deltaColor = if (isPositive) green700 else red600
    val deltaBg    = if (isPositive) green50 else red50
    val deltaText  = if (isPositive) "+${entry.delta}" else "${entry.delta}"

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(entry.createdAt.take(10), fontSize = 11.sp, color = gray400)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("${entry.previousScore} →", fontSize = 12.sp, color = gray400)
                    Text("${entry.finalScore}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = gray700)
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(deltaBg)
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(deltaText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = deltaColor)
                    }
                }
            }
            entry.reasons?.forEach { reason ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("•", fontSize = 11.sp, color = gray400)
                    Text(reason, fontSize = 11.sp, color = gray700)
                }
            }
        }
    }
}

// ── Create household screen ───────────────────────────────────────────────────

@Composable
private fun CreateHouseholdScreen(onBack: () -> Unit, onSuccess: () -> Unit) {
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken()
        if (token != null) {
            try {
                val response = getCurrentUserHousehold(token)
                val household = response.data.toHouseholdDto()
                if (household != null) {
                    HouseholdStore.setHousehold(household)
                    return@LaunchedEffect
                }
            } catch (_: Exception) { }
        }
        isChecking = false
    }

    if (isChecking) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CreateHouseholdForm(onSuccess = onSuccess, onCancel = onBack)
    }
}
