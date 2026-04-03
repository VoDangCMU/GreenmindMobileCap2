package com.vodang.greenmind.wasteanalytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.fmt
import kotlin.math.roundToInt

// ── Palette ───────────────────────────────────────────────────────────────────
private val blue700   = Color(0xFF1565C0)
private val blue600   = Color(0xFF1976D2)
private val blue50    = Color(0xFFE3F2FD)
private val red700    = Color(0xFFC62828)
private val amber     = Color(0xFFF57F17)
private val teal600   = Color(0xFF00897B)
private val purple700 = Color(0xFF6A1B9A)
private val bgGray    = Color(0xFFF5F5F5)

// ── Mock time-series data ─────────────────────────────────────────────────────

private data class PeriodData(
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
                    Text("📈 Waste Analytics", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = blue700)
                    Text("Household trends · $selectedPeriod view", fontSize = 11.sp, color = Color.Gray)
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
                        "Total Waste",
                        "${totalWaste.fmt(1)} kg",
                        blue600,
                        Modifier.weight(1f)
                    )
                    SummaryChip(
                        "Peak ($peakLabel)",
                        "${peakKg.fmt(1)} kg",
                        Color(0xFFE65100),
                        Modifier.weight(1f)
                    )
                    SummaryChip(
                        "Avg Air",
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
                        title    = "Pollution Impact Trends",
                        subtitle = "Air · Water · Soil",
                        icon     = "💨",
                        seriesList = listOf(
                            SeriesData("Air",   red700,  data.airPollution),
                            SeriesData("Water", blue600, data.waterPollution),
                            SeriesData("Soil",  amber,   data.soilPollution),
                        ),
                        xLabels = data.labels,
                    )
                }

                // ── Pollutants chart ──────────────────────────────────────────
                if (showPollutants) {
                    MultiSeriesCard(
                        title    = "Key Pollutants",
                        subtitle = "CO₂ · Dioxin · Microplastic",
                        icon     = "☁️",
                        seriesList = listOf(
                            SeriesData("CO₂",         red700,   data.co2),
                            SeriesData("Dioxin",       purple700, data.dioxin),
                            SeriesData("Microplastic", teal600,  data.microplastic),
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
                        Text("📊 Insight", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = blue700)
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

// ── Waste volume card (single-series smooth bezier area) ──────────────────────

@Composable
private fun WasteVolumeCard(data: PeriodData) {
    var enabled by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(blue50),
                    contentAlignment = Alignment.Center
                ) { Text("🗑️", fontSize = 18.sp) }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Waste Volume", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                    Text("Kilograms", fontSize = 11.sp, color = Color.Gray)
                }
                // Single-series toggle
                LegendItem(label = "Waste (kg)", color = blue600, enabled = enabled, onClick = { enabled = !enabled })
            }

            if (!enabled) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    Text("Series hidden", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
            val values = data.wasteKg
            val n = values.size

            BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                val density  = LocalDensity.current
                val widthPx  = with(density) { maxWidth.toPx() }
                val heightPx = with(density) { maxHeight.toPx() }
                val topPad   = with(density) { 24.dp.toPx() }
                val botPad   = with(density) { 20.dp.toPx() }
                val chartH   = heightPx - topPad - botPad
                val maxVal   = values.max()
                val minVal   = (values.min() * 0.85f).coerceAtLeast(0f)
                val range    = (maxVal - minVal).coerceAtLeast(0.01f)

                fun xOf(i: Int) = widthPx * (i + 0.5f) / n
                fun yOf(v: Float) = topPad + chartH * (1f - (v - minVal) / range)

                val pts = values.mapIndexed { i, v -> Offset(xOf(i), yOf(v)) }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    // Filled area (cubic bezier)
                    val fill = Path().apply {
                        moveTo(pts[0].x, pts[0].y)
                        for (i in 1 until pts.size) {
                            val mx = (pts[i - 1].x + pts[i].x) / 2f
                            cubicTo(mx, pts[i - 1].y, mx, pts[i].y, pts[i].x, pts[i].y)
                        }
                        lineTo(pts.last().x, heightPx - botPad)
                        lineTo(pts.first().x, heightPx - botPad)
                        close()
                    }
                    drawPath(fill, Brush.verticalGradient(
                        listOf(blue600.copy(alpha = 0.25f), blue600.copy(alpha = 0.02f)),
                        startY = topPad, endY = heightPx - botPad
                    ))
                    // Smooth line
                    val line = Path().apply {
                        moveTo(pts[0].x, pts[0].y)
                        for (i in 1 until pts.size) {
                            val mx = (pts[i - 1].x + pts[i].x) / 2f
                            cubicTo(mx, pts[i - 1].y, mx, pts[i].y, pts[i].x, pts[i].y)
                        }
                    }
                    drawPath(line, blue600, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                    // Dots
                    pts.forEachIndexed { i, pt ->
                        val isLast = i == pts.lastIndex
                        drawCircle(Color.White, 5.dp.toPx(), pt)
                        drawCircle(
                            if (isLast) blue700 else blue600,
                            if (isLast) 5.5.dp.toPx() else 4.5.dp.toPx(),
                            pt,
                            style = Stroke(2.dp.toPx())
                        )
                        if (isLast) drawCircle(blue600.copy(alpha = 0.18f), 10.dp.toPx(), pt)
                    }
                }

                // Value labels — only show if not too crowded (≤8 pts)
                if (n <= 8) {
                    values.forEachIndexed { i, v ->
                        val isLast = i == values.lastIndex
                        Text(
                            text = "${v.fmt(1)}kg",
                            modifier = Modifier.offset {
                                IntOffset(
                                    x = (xOf(i) - 14.dp.toPx()).roundToInt(),
                                    y = (yOf(v) - 20.dp.toPx()).roundToInt()
                                )
                            },
                            fontSize = 9.sp,
                            fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                            color = blue600
                        )
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    data.labels.forEach { Text(it, fontSize = 9.sp, color = Color.Gray) }
                }
            }
            } // end else (enabled)
        }
    }
}

// ── Multi-series area chart card (shared for impact + pollutants) ─────────────

private data class SeriesData(val label: String, val color: Color, val values: List<Float>)

@Composable
private fun MultiSeriesCard(
    title: String,
    subtitle: String,
    icon: String,
    seriesList: List<SeriesData>,
    xLabels: List<String>,
) {
    val textMeasurer = rememberTextMeasurer()
    val n = xLabels.size

    // Track which series are enabled; reset when the series list changes (period switch)
    var enabledLabels by remember(seriesList) { mutableStateOf(seriesList.map { it.label }.toSet()) }
    val active = seriesList.filter { it.label in enabledLabels }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(seriesList.first().color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) { Text(icon, fontSize = 18.sp) }
                Column {
                    Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                    Text(subtitle, fontSize = 11.sp, color = Color.Gray)
                }
            }

            // Clickable legend — tap to toggle each series
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                seriesList.forEach { s ->
                    val enabled = s.label in enabledLabels
                    LegendItem(
                        label   = s.label,
                        color   = s.color,
                        enabled = enabled,
                        onClick = {
                            // Prevent disabling the last active series
                            if (enabled && enabledLabels.size > 1) enabledLabels = enabledLabels - s.label
                            else if (!enabled)                      enabledLabels = enabledLabels + s.label
                        }
                    )
                }
            }

            if (active.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(60.dp), contentAlignment = Alignment.Center) {
                    Text("No series selected", fontSize = 12.sp, color = Color.Gray)
                }
            } else {
                // Chart canvas
                Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    val allValues = active.flatMap { it.values }
                    val maxVal = (allValues.maxOrNull() ?: 0f).coerceAtLeast(0.01f) * 1.25f
                    val w = size.width
                    val h = size.height
                    val padTop  = 28f
                    val padLeft = 36f
                    val chartW  = w - padLeft

                    fun xOf(i: Int) = padLeft + if (n <= 1) chartW / 2f else i * chartW / (n - 1).toFloat()
                    fun yOf(v: Float) = h - (v / maxVal) * (h - padTop)

                    // Y-axis ruler
                    val axisStyle = TextStyle(fontSize = 8.sp, color = Color(0xFF9E9E9E))
                    repeat(5) { g ->
                        val frac = g.toFloat() / 4f
                        val yG   = padTop + (1f - frac) * (h - padTop)
                        val tick = frac * maxVal
                        drawLine(Color(0xFFBDBDBD), Offset(padLeft - 4f, yG), Offset(padLeft, yG), 1f)
                        val lbl = "%.2f".format(tick)
                        val msr = textMeasurer.measure(lbl, axisStyle)
                        drawText(msr, topLeft = Offset((padLeft - 6f - msr.size.width).coerceAtLeast(0f), yG - msr.size.height / 2f))
                    }
                    drawLine(Color(0xFFBDBDBD), Offset(padLeft, padTop), Offset(padLeft, h), 1.5f)

                    // Grid
                    repeat(5) { g ->
                        drawLine(Color(0xFFEEEEEE), Offset(padLeft, padTop + g * (h - padTop) / 4f), Offset(w, padTop + g * (h - padTop) / 4f), 1f)
                    }

                    // Draw active series back-to-front
                    active.reversed().forEach { series ->
                        val vals = series.values
                        val c    = series.color

                        val area = Path().apply {
                            moveTo(xOf(0), h)
                            vals.forEachIndexed { i, v -> lineTo(xOf(i), yOf(v)) }
                            lineTo(xOf(vals.size - 1), h)
                            close()
                        }
                        drawPath(area, Brush.verticalGradient(listOf(c.copy(alpha = 0.28f), c.copy(alpha = 0.03f)), 0f, h))

                        val line = Path().apply {
                            vals.forEachIndexed { i, v -> if (i == 0) moveTo(xOf(i), yOf(v)) else lineTo(xOf(i), yOf(v)) }
                        }
                        drawPath(line, c, style = Stroke(width = 3f))

                        val labelStyle = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.SemiBold, color = c)
                        vals.forEachIndexed { i, v ->
                            val cx = xOf(i); val cy = yOf(v)
                            drawCircle(c, 5f, Offset(cx, cy))
                            drawCircle(Color.White, 3f, Offset(cx, cy))
                            if (n <= 7) {
                                val lbl = "%.2f".format(v)
                                val msr = textMeasurer.measure(lbl, labelStyle)
                                val lx = (cx - msr.size.width / 2f).coerceIn(0f, w - msr.size.width)
                                val ly = (cy - msr.size.height - 6f).coerceAtLeast(0f)
                                drawText(msr, topLeft = Offset(lx, ly))
                            }
                        }
                    }
                }

                // X-axis labels
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 36.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    xLabels.forEach { Text(it, fontSize = 9.sp, color = Color.Gray) }
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun PeriodChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) blue600 else Color(0xFFEEEEEE))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF757575)
        )
    }
}

@Composable
private fun MetricChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (selected) blue600.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) blue600 else Color(0xFF9E9E9E)
        )
    }
}

@Composable
private fun SummaryChip(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, fontSize = 10.sp, color = color.copy(alpha = 0.7f))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun LegendItem(
    label: String,
    color: Color,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val effectiveColor = if (enabled) color else Color(0xFFBDBDBD)
    Row(
        modifier = if (onClick != null) Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        else Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp, 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(effectiveColor)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (enabled) Color(0xFF424242) else Color(0xFFBDBDBD),
            fontWeight = if (enabled) FontWeight.Normal else FontWeight.Normal,
            style = androidx.compose.ui.text.TextStyle(
                textDecoration = if (enabled) null else androidx.compose.ui.text.style.TextDecoration.LineThrough
            )
        )
    }
}
