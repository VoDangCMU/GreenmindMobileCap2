package com.vodang.greenmind.wasteanalytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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

// ── Mock time-series data ─────────────────────────────────────────────────────

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

private val dayData = PeriodData(
    labels         = listOf("6am", "9am", "12pm", "3pm", "6pm", "9pm"),
    wasteKg        = listOf(0.1f, 0.3f, 0.8f, 0.5f, 1.2f, 0.4f),
    airPollution   = listOf(0.10f, 0.18f, 0.35f, 0.25f, 0.48f, 0.20f),
    waterPollution = listOf(0.04f, 0.07f, 0.14f, 0.10f, 0.19f, 0.08f),
    soilPollution  = listOf(0.12f, 0.22f, 0.42f, 0.30f, 0.58f, 0.24f),
    co2            = listOf(0.12f, 0.28f, 0.68f, 0.45f, 0.82f, 0.35f),
    dioxin         = listOf(0.08f, 0.22f, 0.64f, 0.40f, 0.78f, 0.30f),
    microplastic   = listOf(0.10f, 0.25f, 0.67f, 0.42f, 0.80f, 0.32f),
)

private val weekData = PeriodData(
    labels         = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
    wasteKg        = listOf(1.2f, 0.8f, 2.1f, 1.5f, 0.9f, 2.8f, 1.1f),
    airPollution   = listOf(0.25f, 0.18f, 0.38f, 0.28f, 0.15f, 0.42f, 0.22f),
    waterPollution = listOf(0.10f, 0.07f, 0.15f, 0.11f, 0.06f, 0.17f, 0.09f),
    soilPollution  = listOf(0.30f, 0.22f, 0.45f, 0.33f, 0.18f, 0.51f, 0.27f),
    co2            = listOf(0.52f, 0.38f, 0.85f, 0.64f, 0.32f, 0.92f, 0.48f),
    dioxin         = listOf(0.48f, 0.35f, 0.80f, 0.60f, 0.30f, 0.87f, 0.44f),
    microplastic   = listOf(0.50f, 0.37f, 0.82f, 0.62f, 0.31f, 0.90f, 0.46f),
)

private val monthData = PeriodData(
    labels         = listOf("W1", "W2", "W3", "W4"),
    wasteKg        = listOf(8.5f, 6.2f, 11.3f, 9.7f),
    airPollution   = listOf(0.28f, 0.21f, 0.38f, 0.30f),
    waterPollution = listOf(0.11f, 0.08f, 0.15f, 0.12f),
    soilPollution  = listOf(0.33f, 0.25f, 0.46f, 0.36f),
    co2            = listOf(0.65f, 0.50f, 0.88f, 0.72f),
    dioxin         = listOf(0.60f, 0.46f, 0.83f, 0.67f),
    microplastic   = listOf(0.62f, 0.48f, 0.85f, 0.70f),
)

private val yearData = PeriodData(
    labels         = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"),
    wasteKg        = listOf(28f, 22f, 35f, 31f, 27f, 33f, 38f, 30f, 26f, 32f, 29f, 25f),
    airPollution   = listOf(0.30f, 0.24f, 0.38f, 0.32f, 0.27f, 0.35f, 0.42f, 0.31f, 0.26f, 0.34f, 0.29f, 0.25f),
    waterPollution = listOf(0.12f, 0.09f, 0.15f, 0.13f, 0.11f, 0.14f, 0.17f, 0.12f, 0.10f, 0.14f, 0.11f, 0.10f),
    soilPollution  = listOf(0.36f, 0.28f, 0.46f, 0.38f, 0.32f, 0.42f, 0.50f, 0.37f, 0.31f, 0.41f, 0.35f, 0.30f),
    co2            = listOf(0.72f, 0.58f, 0.90f, 0.78f, 0.65f, 0.85f, 0.95f, 0.75f, 0.62f, 0.82f, 0.70f, 0.60f),
    dioxin         = listOf(0.68f, 0.54f, 0.85f, 0.73f, 0.61f, 0.80f, 0.90f, 0.70f, 0.58f, 0.77f, 0.66f, 0.56f),
    microplastic   = listOf(0.70f, 0.56f, 0.87f, 0.75f, 0.63f, 0.82f, 0.92f, 0.72f, 0.60f, 0.79f, 0.68f, 0.58f),
)

private val periodMap = mapOf(
    "Day"   to dayData,
    "Week"  to weekData,
    "Month" to monthData,
    "Year"  to yearData,
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun WasteAnalyticsScreen() {
    val s = LocalAppStrings.current
    var selectedPeriod by remember { mutableStateOf("Week") }
    var selectedMetric by remember { mutableStateOf("All") }
    val data = periodMap[selectedPeriod] ?: weekData

    val showWaste      = selectedMetric == "All" || selectedMetric == "Waste (kg)"
    val showImpact     = selectedMetric == "All" || selectedMetric == "Impact"
    val showPollutants = selectedMetric == "All" || selectedMetric == "Pollutants"

    val totalWaste = data.wasteKg.sum()
    val avgAir     = data.airPollution.average().toFloat()
    val avgCo2     = data.co2.average().toFloat()
    val peakKg     = data.wasteKg.max()
    val peakLabel  = data.labels[data.wasteKg.indexOfFirst { it == peakKg }]

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
                Column {
                    Text(s.wasteAnalyticsTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = blue700)
                    Text(s.householdTrends(selectedPeriod), fontSize = 11.sp, color = Color.Gray)
                }
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
                        icon     = Icons.Filled.Analytics.name,
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
                        icon     = Icons.Filled.Cloud.name,
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
