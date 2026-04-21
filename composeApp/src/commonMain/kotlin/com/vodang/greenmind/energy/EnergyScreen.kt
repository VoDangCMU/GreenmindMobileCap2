package com.vodang.greenmind.energy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.Icon
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
import androidx.compose.foundation.Canvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.fmt
import kotlin.math.roundToInt

private val blue700  = Color(0xFF1565C0)
private val blue600  = Color(0xFF1976D2)
private val blue50   = Color(0xFFE3F2FD)
private val blue100  = Color(0xFFBBDEFB)
private val bgGray   = Color(0xFFF5F5F5)

@Composable
fun EnergyScreen() {
    val s = LocalAppStrings.current

    // TODO: Replace hardcoded energy data with real readings from the API.
    //       Expected source: GET /energy/readings?range=week  → List<Float> (kWh per day)
    //                        GET /energy/readings?range=month → monthTotal: Float
    //       Wire into an EnergyStore with StateFlow so EnergyScreen stays stateless.
    val weekValues = listOf(3.8f, 4.1f, 3.5f, 5.2f, 4.8f, 6.1f, 4.2f)
    val todayKwh   = weekValues.last()
    val weekTotal  = weekValues.sum()
    val dailyAvg   = weekTotal / weekValues.size
    val monthTotal = 127.4f

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGray)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            // ── Hero card: today ──────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = blue600),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(blue700, blue600, Color(0xFF42A5F5))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(s.energyTodayLabel, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                todayKwh.fmt(1),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                lineHeight = 52.sp
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                s.kWh,
                                fontSize = 18.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            // TODO: Calculate real delta vs yesterday from the API data. Move string to i18n.
                        Text(s.vsYesterday("5%"), fontSize = 12.sp, color = Color.White)
                        }
                    }
                    Icon(
                        imageVector = Icons.Filled.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.align(Alignment.CenterEnd),
                        tint = Color.White.copy(alpha = 0.3f),
                    )
                }
            }

            // ── Stats row ─────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatChip(
                    label = s.energyMonthTotal,
                    value = "${monthTotal.fmt(1)} kWh",
                    modifier = Modifier.weight(1f)
                )
                StatChip(
                    label = s.energyAvgDaily,
                    value = "${dailyAvg.fmt(1)} kWh",
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Weekly chart ──────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(s.electricityWeekTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Spacer(Modifier.weight(1f))
                        Text(s.kWh, fontSize = 11.sp, color = Color.Gray)
                    }
                    Spacer(Modifier.height(16.dp))
                    WeekChart(values = weekValues, labels = s.weekDays)
                    Spacer(Modifier.height(8.dp))
                    Text(s.electricityWeekSummary, fontSize = 11.sp, color = Color.Gray)
                }
            }

            // ── Tips ──────────────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(s.energyTips, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    s.energyTipsList.forEach { tip ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(blue50)
                                .padding(10.dp),
                        ) {
                            Text(tip, fontSize = 13.sp, color = Color(0xFF1B1B1B), lineHeight = 18.sp)
                        }
                    }
                }
            }
        }
}

@Composable
private fun StatChip(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Spacer(Modifier.height(2.dp))
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = blue600)
        }
    }
}

@Composable
private fun WeekChart(values: List<Float>, labels: List<String>) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(160.dp)) {
        val density   = LocalDensity.current
        val widthPx   = with(density) { maxWidth.toPx() }
        val heightPx  = with(density) { maxHeight.toPx() }
        val n         = values.size
        val topPad    = with(density) { 24.dp.toPx() }
        val bottomPad = with(density) { 20.dp.toPx() }
        val chartH    = heightPx - topPad - bottomPad
        val maxVal    = values.max()
        val minVal    = values.min() * 0.85f
        val range     = maxVal - minVal

        fun xOf(i: Int) = widthPx * (i + 0.5f) / n
        fun yOf(v: Float) = topPad + chartH * (1f - (v - minVal) / range)

        val pts = values.mapIndexed { i, v -> Offset(xOf(i), yOf(v)) }

        Canvas(modifier = Modifier.fillMaxSize()) {
            val fillPath = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val midX = (pts[i - 1].x + pts[i].x) / 2f
                    cubicTo(midX, pts[i - 1].y, midX, pts[i].y, pts[i].x, pts[i].y)
                }
                lineTo(pts.last().x, heightPx - bottomPad)
                lineTo(pts.first().x, heightPx - bottomPad)
                close()
            }
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    listOf(blue600.copy(alpha = 0.25f), blue600.copy(alpha = 0.02f)),
                    startY = topPad,
                    endY = heightPx - bottomPad
                )
            )
            val linePath = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val midX = (pts[i - 1].x + pts[i].x) / 2f
                    cubicTo(midX, pts[i - 1].y, midX, pts[i].y, pts[i].x, pts[i].y)
                }
            }
            drawPath(linePath, color = blue600, style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            pts.forEachIndexed { i, pt ->
                val isToday = i == pts.lastIndex
                drawCircle(Color.White, radius = 5.dp.toPx(), center = pt)
                drawCircle(
                    if (isToday) blue700 else blue600,
                    radius = if (isToday) 5.5.dp.toPx() else 4.5.dp.toPx(),
                    center = pt,
                    style = Stroke(width = 2.dp.toPx())
                )
                if (isToday) drawCircle(blue600.copy(alpha = 0.2f), radius = 10.dp.toPx(), center = pt)
            }
        }

        values.forEachIndexed { i, v ->
            Text(
                text = v.fmt(1),
                modifier = Modifier.offset {
                    IntOffset(
                        x = (xOf(i) - 12.dp.toPx()).roundToInt(),
                        y = (yOf(v) - 20.dp.toPx()).roundToInt()
                    )
                },
                fontSize = 10.sp,
                fontWeight = if (i == values.lastIndex) FontWeight.Bold else FontWeight.Normal,
                color = blue600
            )
        }

        Row(
            modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            labels.forEach { label ->
                Text(label, fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}
