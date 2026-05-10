package com.vodang.greenmind.wasteanalytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.wasteanalytics.components.MultiSeriesCard
import com.vodang.greenmind.wasteanalytics.components.PeriodChip
import com.vodang.greenmind.wasteanalytics.components.MetricChip
import com.vodang.greenmind.wasteanalytics.components.SeriesData
import com.vodang.greenmind.wasteanalytics.components.SummaryChip
import com.vodang.greenmind.wasteanalytics.components.WasteVolumeCard

private val bgGray    = Color(0xFFF5F5F5)
private val blue700   = Color(0xFF1565C0)
private val blue600   = Color(0xFF1976D2)
private val blue50    = Color(0xFFE3F2FD)
private val red700    = Color(0xFFC62828)
private val amber     = Color(0xFFF57F17)
private val teal600  = Color(0xFF00897B)
private val purple700 = Color(0xFF6A1B9A)

// ── Data Processing ─────────────────────────────────────────────────────────────

internal data class PeriodData(
    val labels: List<String>,
    val wasteKg: List<Float>,
    val airPollution: List<Float>,
    val waterPollution: List<Float>,
    val soilPollution: List<Float>,
    val co2: List<Float>,
    val dioxin: List<Float>,
    val microplastic: List<Float>,
)

private data class ParsedDate(val year: Int, val month: Int, val day: Int, val hour: Int)

private fun parseDate(iso: String?): ParsedDate {
    if (iso == null || iso.length < 19) return ParsedDate(0, 0, 0, 0)
    return try {
        ParsedDate(
            year = iso.substring(0, 4).toInt(),
            month = iso.substring(5, 7).toInt(),
            day = iso.substring(8, 10).toInt(),
            hour = iso.substring(11, 13).toInt()
        )
    } catch (e: Exception) {
        ParsedDate(0, 0, 0, 0)
    }
}

private fun List<Double>.avgOrZero(): Float {
    if (this.isEmpty()) return 0f
    val avg = this.average()
    return if (avg.isNaN()) 0f else avg.toFloat()
}

private fun buildPeriodData(labels: List<String>, buckets: Array<MutableList<com.vodang.greenmind.api.households.DetectTrashHistoryDto>>): PeriodData {
    return PeriodData(
        labels = labels,
        wasteKg = buckets.map { list -> list.sumOf { it.totalMassKg ?: 0.0 }.toFloat() },
        airPollution = buckets.map { list -> list.mapNotNull { it.impact?.airPollution }.avgOrZero() },
        waterPollution = buckets.map { list -> list.mapNotNull { it.impact?.waterPollution }.avgOrZero() },
        soilPollution = buckets.map { list -> list.mapNotNull { it.impact?.soilPollution }.avgOrZero() },
        co2 = buckets.map { list -> list.mapNotNull { it.pollution?.co2 }.avgOrZero() },
        dioxin = buckets.map { list -> list.mapNotNull { it.pollution?.dioxin }.avgOrZero() },
        microplastic = buckets.map { list -> list.mapNotNull { it.pollution?.microplastic }.avgOrZero() }
    )
}

private fun processData(raw: List<com.vodang.greenmind.api.households.DetectTrashHistoryDto>, period: String): PeriodData {
    val emptyPeriod = PeriodData(emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
    if (raw.isEmpty()) return emptyPeriod
    
    val sorted = raw.sortedBy { it.createdAt.orEmpty() }
    
    return when (period) {
        "Day" -> {
            val labels = listOf("12am", "3am", "6am", "9am", "12pm", "3pm", "6pm", "9pm")
            val buckets = Array(8) { mutableListOf<com.vodang.greenmind.api.households.DetectTrashHistoryDto>() }
            sorted.forEach {
                val pd = parseDate(it.createdAt)
                val idx = (pd.hour / 3).coerceIn(0, 7)
                buckets[idx].add(it)
            }
            buildPeriodData(labels, buckets)
        }
        "Week" -> {
            val allDays = sorted.map { parseDate(it.createdAt) }.distinctBy { "${it.year}-${it.month}-${it.day}" }
            val last7 = allDays.takeLast(7)
            val labels = last7.map { "${it.day}/${it.month}" }
            val buckets = Array(last7.size) { mutableListOf<com.vodang.greenmind.api.households.DetectTrashHistoryDto>() }
            sorted.forEach { item ->
                val pd = parseDate(item.createdAt)
                val idx = last7.indexOfFirst { it.year == pd.year && it.month == pd.month && it.day == pd.day }
                if (idx != -1) buckets[idx].add(item)
            }
            if (labels.isEmpty()) buildPeriodData(listOf("No Data"), Array(1) { mutableListOf() })
            else buildPeriodData(labels, buckets)
        }
        "Month" -> {
            val labels = listOf("W1", "W2", "W3", "W4")
            val buckets = Array(4) { mutableListOf<com.vodang.greenmind.api.households.DetectTrashHistoryDto>() }
            sorted.forEach {
                val pd = parseDate(it.createdAt)
                val idx = when (pd.day) {
                    in 1..7 -> 0
                    in 8..14 -> 1
                    in 15..21 -> 2
                    else -> 3
                }
                buckets[idx].add(it)
            }
            buildPeriodData(labels, buckets)
        }
        "Year" -> {
            val labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val buckets = Array(12) { mutableListOf<com.vodang.greenmind.api.households.DetectTrashHistoryDto>() }
            sorted.forEach {
                val pd = parseDate(it.createdAt)
                val idx = (pd.month - 1).coerceIn(0, 11)
                buckets[idx].add(it)
            }
            buildPeriodData(labels, buckets)
        }
        else -> emptyPeriod
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun WasteAnalyticsScreen(onBack: () -> Unit = {}) {
    val s = LocalAppStrings.current
    var selectedPeriod by remember { mutableStateOf("Week") }
    var selectedMetric by remember { mutableStateOf("All") }
    
    var isLoading by remember { mutableStateOf(true) }
    var rawData by remember { mutableStateOf<List<com.vodang.greenmind.api.households.DetectTrashHistoryDto>>(emptyList()) }
    
    LaunchedEffect(Unit) {
        val token = com.vodang.greenmind.store.SettingsStore.getAccessToken()
        if (token != null) {
            try {
                isLoading = true
                val response = com.vodang.greenmind.api.households.getDetectsMonthly(token)
                rawData = response.data
            } catch (e: Exception) {
                // Ignore or handle error
            } finally {
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }
    
    val data = remember(rawData, selectedPeriod) { processData(rawData, selectedPeriod) }

    val showWaste      = selectedMetric == "All" || selectedMetric == "Waste (kg)"
    val showImpact     = selectedMetric == "All" || selectedMetric == "Impact"
    val showPollutants = selectedMetric == "All" || selectedMetric == "Pollutants"

    val totalWaste = data.wasteKg.sum()
    val avgAir     = data.airPollution.average().toFloat().takeIf { !it.isNaN() } ?: 0f
    val avgCo2     = data.co2.average().toFloat().takeIf { !it.isNaN() } ?: 0f
    val peakKg     = data.wasteKg.maxOrNull() ?: 0f
    val peakLabel  = data.labels.getOrNull(data.wasteKg.indexOfFirst { it == peakKg }.coerceAtLeast(0)) ?: ""

    Box(modifier = Modifier.fillMaxSize().background(bgGray)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // ── Top bar ───────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = blue700)
                }
                Column(modifier = Modifier.padding(start = 8.dp)) {
                    Text(s.wasteAnalyticsTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = blue700)
                    Text(s.householdTrends(selectedPeriod), fontSize = 11.sp, color = Color.Gray)
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = blue600)
                }
                return@Column
            }

            if (data.labels.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(top = 100.dp), contentAlignment = Alignment.Center) {
                    Text(s.wasteImpactNoData, color = Color.Gray, fontSize = 14.sp)
                }
                return@Column
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Period selector ───────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Day", "Week", "Month", "Year").forEach { p ->
                        PeriodChip(label = p, selected = selectedPeriod == p) { selectedPeriod = p }
                    }
                }

                // ── Metric filter ─────────────────────────────────────────────
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Waste (kg)", "Impact", "Pollutants").forEach { m ->
                        MetricChip(label = m, selected = selectedMetric == m) { selectedMetric = m }
                    }
                }

                // ── Summary chips ─────────────────────────────────────────────
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SummaryChip(
                        s.totalWaste,
                        "${totalWaste.fmt(1)} kg",
                        blue600,
                        Modifier.weight(1f)
                    )
                    SummaryChip(
                        s.peak(peakLabel),
                        "${peakKg.fmt(1)} kg",
                        Color(0xFFE65100),
                        Modifier.weight(1f)
                    )
                    SummaryChip(
                        s.avgAir,
                        "${(avgAir * 100).roundToInt()}%",
                        red700,
                        Modifier.weight(1f)
                    )
                }

                // ── Waste volume chart ────────────────────────────────────────
                if (showWaste) {
                    WasteVolumeCard(data = data)
                }

                // ── Impact trends chart ───────────────────────────────────────
                if (showImpact) {
                    MultiSeriesCard(
                        title    = s.pollutionImpactTrends,
                        subtitle = s.airWaterSoil,
                        icon     = Icons.Filled.Analytics,
                        seriesList = listOf(
                            SeriesData(s.seriesAir,   red700,  data.airPollution),
                            SeriesData(s.seriesWater, blue600, data.waterPollution),
                            SeriesData(s.seriesSoil,  amber,   data.soilPollution),
                        ),
                        xLabels = data.labels,
                    )
                }

                // ── Pollutants chart ──────────────────────────────────────────
                if (showPollutants) {
                    MultiSeriesCard(
                        title    = s.keyPollutants,
                        subtitle = s.co2DioxinMicroplastic,
                        icon     = Icons.Filled.Cloud,
                        seriesList = listOf(
                            SeriesData(s.seriesCO2,         red700,   data.co2),
                            SeriesData(s.seriesDioxin,       purple700, data.dioxin),
                            SeriesData(s.seriesMicroplastic, teal600,  data.microplastic),
                        ),
                        xLabels = data.labels,
                    )
                }

                // ── Avg CO₂ insight box ───────────────────────────────────────
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(blue50)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(s.insight, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = blue700)
                        Text(
                            buildString {
                                append("Over this $selectedPeriod, avg CO₂ was ${avgCo2.fmt(2)}, ")
                                append("total waste reached ${totalWaste.fmt(1)} kg, ")
                                append("with peak on $peakLabel at ${peakKg.fmt(1)} kg.")
                            },
                            fontSize = 11.sp,
                            color = blue700.copy(alpha = 0.85f),
                            lineHeight = 17.sp
                        )
                    }
                }

                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}
