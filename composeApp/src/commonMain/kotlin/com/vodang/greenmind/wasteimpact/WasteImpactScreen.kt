package com.vodang.greenmind.wasteimpact

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.DetectTrashHistoryDto
import com.vodang.greenmind.api.households.getDetectHistoryByHousehold
import com.vodang.greenmind.api.households.getDetectHistoryByUser
import com.vodang.greenmind.api.wastedetect.WasteDetectImpact
import com.vodang.greenmind.api.wastedetect.WasteDetectItem
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WasteSortStore
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastesort.WasteSortEntry
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ── Palette ───────────────────────────────────────────────────────────────────
private val orange700  = Color(0xFFE65100)
private val orange600  = Color(0xFFF4511E)
private val orange400  = Color(0xFFFF7043)
private val orange50   = Color(0xFFFFF3E0)
private val green800   = Color(0xFF2E7D32)
private val green50    = Color(0xFFE8F5E9)
private val red600     = Color(0xFFE53935)
private val blue600    = Color(0xFF1976D2)
private val amber      = Color(0xFFF57F17)
private val bgGray     = Color(0xFFF5F5F5)

/** Friendly display names for raw pollution keys. */
private val pollutantLabel = mapOf(
    "CO2"               to "CO₂",
    "dioxin"            to "Dioxin",
    "microplastic"      to "Microplastic",
    "toxic_chemicals"   to "Toxic chemicals",
    "non_biodegradable" to "Non-biodegradable",
    "NOx"               to "NOₓ",
    "SO2"               to "SO₂",
    "CH4"               to "CH₄",
    "PM2.5"             to "PM2.5",
    "Pb"                to "Lead (Pb)",
    "Hg"                to "Mercury (Hg)",
    "Cd"                to "Cadmium (Cd)",
    "nitrate"           to "Nitrate",
    "chemical_residue"  to "Chemical residue",
    "styrene"           to "Styrene",
)

// ── Converter ─────────────────────────────────────────────────────────────────

private fun DetectTrashHistoryDto.toWasteSortEntry(): WasteSortEntry {
    val pollutionMap: Map<String, Double>? = pollution?.let { p ->
        buildMap {
            p.cd?.let          { put("Cd", it.toDouble()) }
            p.hg?.let          { put("Hg", it.toDouble()) }
            p.pb?.let          { put("Pb", it.toDouble()) }
            p.ch4?.let         { put("CH4", it.toDouble()) }
            p.co2?.let         { put("CO2", it) }
            p.nox?.let         { put("NOx", it) }
            p.so2?.let         { put("SO2", it) }
            p.pm25?.let        { put("PM2.5", it.toDouble()) }
            p.dioxin?.let      { put("dioxin", it) }
            p.nitrate?.let     { put("nitrate", it.toDouble()) }
            p.styrene?.let     { put("styrene", it) }
            p.microplastic?.let { put("microplastic", it) }
            p.toxicChemicals?.let   { put("toxic_chemicals", it) }
            p.chemicalResidue?.let  { put("chemical_residue", it.toDouble()) }
            p.nonBiodegradable?.let { put("non_biodegradable", it) }
        }
    }
    val impactData = impact
    val pollutantResult: WasteDetectResponse? =
        if (pollutionMap != null && impactData != null) {
            WasteDetectResponse(
                items        = items?.map { WasteDetectItem(it.name, it.quantity, it.area) } ?: emptyList(),
                totalObjects = totalObjects ?: 0,
                imageUrl     = aiAnalysis ?: annotatedImageUrl ?: imageUrl,
                pollution    = pollutionMap,
                impact       = WasteDetectImpact(
                    airPollution   = impactData.airPollution   ?: 0.0,
                    waterPollution = impactData.waterPollution ?: 0.0,
                    soilPollution  = impactData.soilPollution  ?: 0.0,
                ),
            )
        } else null

    return WasteSortEntry(
        id           = id,
        imageUrl     = aiAnalysis ?: annotatedImageUrl ?: imageUrl,
        totalObjects = totalObjects ?: 0,
        grouped      = emptyMap(),
        createdAt    = createdAt?.take(10) ?: "",
        scannedBy    = detectedBy?.fullName ?: detectedBy?.username ?: "",
        pollutantResult = pollutantResult,
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun WasteImpactScreen() {
    val s         = LocalAppStrings.current
    val scope     = rememberCoroutineScope()
    val token     = SettingsStore.getAccessToken()
    val household by HouseholdStore.household.collectAsState()
    val localEntries by WasteSortStore.entries.collectAsState()

    var apiEntries  by remember { mutableStateOf<List<WasteSortEntry>>(emptyList()) }
    var isLoading   by remember { mutableStateOf(false) }
    var error       by remember { mutableStateOf<String?>(null) }
    var refreshKey  by remember { mutableIntStateOf(0) }
    var selectedEntry by remember { mutableStateOf<WasteSortEntry?>(null) }

    // Fetch household + user scan history from backend
    LaunchedEffect(refreshKey, token) {
        val t = token ?: return@LaunchedEffect
        isLoading = true
        error = null
        try {
            val results = mutableListOf<DetectTrashHistoryDto>()

            // Household history (if household exists)
            if (household != null) {
                runCatching { getDetectHistoryByHousehold(t) }
                    .getOrNull()?.data?.let { results += it }
            }

            // User personal history (always)
            runCatching { getDetectHistoryByUser(t) }
                .getOrNull()?.data?.let { results += it }

            // Dedup by id, keep only records that have pollution data (predict_pollutant_impact)
            val seen = mutableSetOf<String>()
            apiEntries = results
                .filter { it.detectType == "predict_pollutant_impact" || it.pollution != null }
                .filter { seen.add(it.id) }
                .map { it.toWasteSortEntry() }
                .sortedByDescending { it.createdAt }
        } catch (e: Throwable) {
            error = e.message ?: s.wasteImpactError
        } finally {
            isLoading = false
        }
    }

    // Merge: API entries + local entries not yet on server (by id dedup, API wins)
    val apiIds = apiEntries.map { it.id }.toSet()
    val mergedEntries = apiEntries + localEntries.filter { it.id !in apiIds }

    val current = selectedEntry
    if (current != null) {
        ScanDetailScreen(entry = current, onBack = { selectedEntry = null })
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGray)
    ) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = orange600)
            }

            error != null && mergedEntries.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("😕", fontSize = 40.sp)
                Text(error!!, fontSize = 13.sp, color = Color.Gray, textAlign = TextAlign.Center)
                Button(
                    onClick = { refreshKey++ },
                    colors = ButtonDefaults.buttonColors(containerColor = orange600),
                    shape = RoundedCornerShape(10.dp),
                ) { Text(s.wasteImpactRetry) }
            }

            mergedEntries.isEmpty() -> Column(
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("📊", fontSize = 48.sp)
                Text(s.wasteImpactNoData, fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }

            else -> WasteImpactContent(
                entries      = mergedEntries,
                onEntryClick = { selectedEntry = it },
                onRefresh    = { refreshKey++ },
            )
        }
    }
}

// ── Content ───────────────────────────────────────────────────────────────────

@Composable
private fun WasteImpactContent(
    entries: List<WasteSortEntry>,
    onEntryClick: (WasteSortEntry) -> Unit,
    onRefresh: () -> Unit = {},
) {
    val s = LocalAppStrings.current

    // Only entries that have pollutant data
    val withImpact = entries.filter { it.pollutantResult != null }

    val totalScans   = entries.size
    val totalItems   = entries.sumOf { it.totalObjects }
    val avgEcoScore  = if (withImpact.isEmpty()) null
                       else withImpact.mapNotNull { it.pollutantResult?.ecoScore }.average().roundToInt()

    // Average air / water / soil across scans that have impact data
    val avgAir   = if (withImpact.isEmpty()) null
                   else withImpact.mapNotNull { it.pollutantResult?.impact?.airPollution }.average()
    val avgWater = if (withImpact.isEmpty()) null
                   else withImpact.mapNotNull { it.pollutantResult?.impact?.waterPollution }.average()
    val avgSoil  = if (withImpact.isEmpty()) null
                   else withImpact.mapNotNull { it.pollutantResult?.impact?.soilPollution }.average()

    // All known pollutant keys in display order
    val allPollutantKeys = listOf(
        "CO2", "CH4", "PM2.5", "NOx", "SO2",
        "Pb", "Hg", "Cd",
        "nitrate", "chemical_residue", "microplastic",
        "dioxin", "toxic_chemicals", "non_biodegradable", "styrene",
    )

    // Aggregate pollutants — average values across all scans
    val aggregatedMap: Map<String, Double> = run {
        val map = mutableMapOf<String, Double>()
        allPollutantKeys.forEach { k -> map[k] = 0.0 }
        if (withImpact.isNotEmpty()) {
            withImpact.forEach { entry ->
                entry.pollutantResult?.pollution?.forEach { (k, v) ->
                    map[k] = (map[k] ?: 0.0) + v
                }
            }
            val count = withImpact.size.toDouble()
            map.keys.toList().forEach { k -> map[k] = (map[k] ?: 0.0) / count }
        }
        map
    }

    // Top active pollutants for the bar chart (non-zero, top 6)
    val aggregatedPollutants: List<Pair<String, Double>> =
        aggregatedMap.entries
            .filter { it.value > 0.0 }
            .sortedByDescending { it.value }
            .take(6)
            .map { it.key to it.value }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ── Hero card ─────────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(orange700, orange600, orange400)))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            "📊 ${s.wasteImpactTitle}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.18f))
                                .clickable { onRefresh() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text("↻ Refresh", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HeroStat("$totalScans", "Scans", Modifier.weight(1f).fillMaxHeight())
                        HeroStat("$totalItems", "Items detected", Modifier.weight(1f).fillMaxHeight())
                        HeroStat(
                            value = if (avgEcoScore != null) "$avgEcoScore%" else "—",
                            label = "Avg eco score",
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        )
                    }
                }
            }
        }

        // ── Eco score breakdown ───────────────────────────────────────────────
        if (avgEcoScore != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Eco Score",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(ecoScoreColor(avgEcoScore).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$avgEcoScore%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ecoScoreColor(avgEcoScore)
                            )
                        }
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            LinearProgressIndicator(
                                progress = { avgEcoScore / 100f },
                                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)),
                                color = ecoScoreColor(avgEcoScore),
                                trackColor = ecoScoreColor(avgEcoScore).copy(alpha = 0.15f)
                            )
                            Text(
                                ecoScoreLabel(avgEcoScore),
                                fontSize = 12.sp,
                                color = ecoScoreColor(avgEcoScore)
                            )
                        }
                    }
                    Text(
                        "Based on ${withImpact.size} scan${if (withImpact.size != 1) "s" else ""} with impact data",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        // ── Avg pollution impact ───────────────────────────────────────────────
        if (avgAir != null && avgWater != null && avgSoil != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Average Pollution Impact",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242)
                    )
                    ImpactMeter("💨", "Air pollution",   avgAir.toFloat())
                    ImpactMeter("💧", "Water pollution", avgWater.toFloat())
                    ImpactMeter("🌱", "Soil pollution",  avgSoil.toFloat())
                }
            }
        }

        // ── Top pollutants ────────────────────────────────────────────────────
        if (aggregatedPollutants.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Top Pollutants",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242)
                    )
                    val maxVal = aggregatedPollutants.first().second
                    aggregatedPollutants.forEachIndexed { idx, (key, value) ->
                        val barColor = when (idx) {
                            0 -> orange700; 1 -> orange600; 2 -> orange400
                            3 -> blue600;   4 -> red600;    else -> Color(0xFF455A64)
                        }
                        PollutantBar(
                            rank     = idx + 1,
                            label    = pollutantLabel[key] ?: key,
                            value    = value,
                            barColor = barColor,
                        )
                    }
                }
            }
        }

        // ── All pollutants table ──────────────────────────────────────────────
        if (withImpact.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Pollutant Breakdown",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF424242)
                    )
                    allPollutantKeys.forEach { key ->
                        val value = aggregatedMap[key] ?: 0.0
                        val active = value > 0.0
                        val labelColor = if (active) {
                            when {
                                value < 0.5 -> green800
                                value <= 0.7 -> amber
                                else -> red600
                            }
                        } else Color(0xFF9E9E9E)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (active) orange700 else Color(0xFFE0E0E0))
                            )
                            Text(
                                pollutantLabel[key] ?: key,
                                fontSize = 12.sp,
                                color = Color(0xFF424242),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                if (active) value.fmt(3) else "0",
                                fontSize = 12.sp,
                                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                color = labelColor
                            )
                        }
                        if (active) {
                            LinearProgressIndicator(
                                progress = { value.toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                                color = labelColor,
                                trackColor = labelColor.copy(alpha = 0.08f)
                            )
                        }
                        if (key != allPollutantKeys.last()) {
                            HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }

        // ── Scan history ─────────────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Scan History",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF424242)
                )
                Spacer(Modifier.height(4.dp))
                entries.forEachIndexed { idx, entry ->
                    ScanHistoryRow(entry = entry, onClick = { onEntryClick(entry) })
                    if (idx < entries.lastIndex) {
                        HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                    }
                }
            }
        }

        // ── Tip footer ────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(green50)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                s.wasteImpactTip,
                fontSize = 12.sp,
                color = green800,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.navigationBarsPadding())
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun HeroStat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.15f))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
    }
}

@Composable
private fun ImpactMeter(icon: String, label: String, value: Float) {
    val progress = value.coerceIn(0f, 1f)
    val pct = (value * 100).roundToInt()
    val barColor = when {
        value < 0.5f -> green800
        value <= 0.7f -> amber
        else -> red600
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 14.sp)
                Text(label, fontSize = 12.sp, color = Color(0xFF424242))
            }
            Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = barColor)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.12f),
        )
    }
}

@Composable
private fun PollutantBar(rank: Int, label: String, value: Double, barColor: Color) {
    val progress = value.toFloat().coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(barColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text("$rank", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = barColor)
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(label, fontSize = 12.sp, color = Color(0xFF212121), fontWeight = FontWeight.Medium)
                Text(
                    value.fmt(2),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = barColor
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = barColor,
                trackColor = barColor.copy(alpha = 0.10f)
            )
        }
    }
}

private fun ecoScoreColor(score: Int): Color = when {
    score >= 60 -> Color(0xFF2E7D32)
    score >= 35 -> Color(0xFFE65100)
    else        -> Color(0xFFC62828)
}

private fun ecoScoreLabel(score: Int): String = when {
    score >= 80 -> "Excellent — minimal impact"
    score >= 60 -> "Good — low impact"
    score >= 35 -> "Fair — moderate impact"
    else        -> "Poor — high environmental impact"
}

// ── Scan history row ──────────────────────────────────────────────────────────

@Composable
private fun ScanHistoryRow(entry: WasteSortEntry, onClick: () -> Unit) {
    val ecoScore = entry.pollutantResult?.ecoScore
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Thumbnail
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF0F0F0))
        ) {
            NetworkImage(
                url = entry.imageUrl,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Info
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                entry.createdAt,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121)
            )
            Text(
                "${entry.totalObjects} items · by ${entry.scannedBy}",
                fontSize = 11.sp,
                color = Color.Gray
            )
        }

        // Eco score badge
        if (ecoScore != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(ecoScoreColor(ecoScore).copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "$ecoScore%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ecoScoreColor(ecoScore)
                )
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFEEEEEE))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("—", fontSize = 12.sp, color = Color.Gray)
            }
        }

        Text("›", fontSize = 18.sp, color = Color(0xFFBDBDBD))
    }
}

// ── Scan detail screen ────────────────────────────────────────────────────────

@Composable
private fun ScanDetailScreen(entry: WasteSortEntry, onBack: () -> Unit) {
    val result = entry.pollutantResult
    val allPollutantKeys = listOf(
        "CO2", "CH4", "PM2.5", "NOx", "SO2",
        "Pb", "Hg", "Cd",
        "nitrate", "chemical_residue", "microplastic",
        "dioxin", "toxic_chemicals", "non_biodegradable", "styrene",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TextButton(onClick = onBack) {
                    Text("← Back", fontSize = 14.sp, color = orange700)
                }
                Column {
                    Text(
                        "Scan Detail",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF212121)
                    )
                    Text(entry.createdAt, fontSize = 11.sp, color = Color.Gray)
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // ── Scan image ────────────────────────────────────────────────
                val imgUrl = result?.imageUrl ?: entry.imageUrl
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    NetworkImage(url = imgUrl, modifier = Modifier.fillMaxSize())
                }

                // ── Summary chip row ──────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SummaryChip("${entry.totalObjects}", "Objects", orange600, Modifier.weight(1f).fillMaxHeight())
                    if (result != null) {
                        SummaryChip("${result.ecoScore}%", "Eco score", ecoScoreColor(result.ecoScore), Modifier.weight(1f).fillMaxHeight())
                    }
                    SummaryChip(entry.scannedBy, "By", Color(0xFF455A64), Modifier.weight(1f).fillMaxHeight())
                }

                // ── Detected items ────────────────────────────────────────────
                if (result != null && result.items.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Detected Items",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            result.items.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(orange600.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("${idx + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = orange600)
                                    }
                                    Text(item.name, fontSize = 13.sp, modifier = Modifier.weight(1f), color = Color(0xFF212121))
                                    Text(
                                        "×${item.quantity}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = orange700
                                    )
                                }
                                if (idx < result.items.lastIndex) {
                                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                // ── Impact ────────────────────────────────────────────────────
                if (result != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Pollution Impact",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            ImpactMeter("💨", "Air pollution",   result.impact.airPollution.toFloat())
                            ImpactMeter("💧", "Water pollution", result.impact.waterPollution.toFloat())
                            ImpactMeter("🌱", "Soil pollution",  result.impact.soilPollution.toFloat())
                        }
                    }
                }

                // ── Pollutant breakdown ───────────────────────────────────────
                if (result != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Pollutant Breakdown",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF424242)
                            )
                            allPollutantKeys.forEach { key ->
                                val value = result.pollution[key] ?: 0.0
                                val active = value > 0.0
                                val labelColor = if (active) orange700 else Color(0xFF9E9E9E)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (active) orange700 else Color(0xFFE0E0E0))
                                    )
                                    Text(
                                        pollutantLabel[key] ?: key,
                                        fontSize = 12.sp,
                                        color = Color(0xFF424242),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        if (active) value.fmt(3) else "0",
                                        fontSize = 12.sp,
                                        fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
                                        color = labelColor
                                    )
                                }
                                if (active) {
                                    LinearProgressIndicator(
                                        progress = { value.toFloat().coerceIn(0f, 1f) },
                                        modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
                                        color = orange700,
                                        trackColor = orange700.copy(alpha = 0.08f)
                                    )
                                }
                                if (key != allPollutantKeys.last()) {
                                    HorizontalDivider(color = Color(0xFFF5F5F5), thickness = 0.5.dp)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun SummaryChip(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color, textAlign = TextAlign.Center)
        Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.8f), textAlign = TextAlign.Center)
    }
}
