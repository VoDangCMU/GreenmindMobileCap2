package com.vodang.greenmind.scandetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import com.vodang.greenmind.fmt
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.scandetail.ScanImpact
import com.vodang.greenmind.scandetail.getImpactColor
import com.vodang.greenmind.scandetail.neutralGray400
import com.vodang.greenmind.scandetail.neutralGray700
import com.vodang.greenmind.scandetail.pollutantGreen
import com.vodang.greenmind.scandetail.pollutantOrange
import com.vodang.greenmind.scandetail.pollutantRed

// ── Impact Meter Section ──────────────────────────────────────────────────────

@Composable
fun ImpactMeterSection(
    impact: ScanImpact?,
    modifier: Modifier = Modifier,
) {
    val s = LocalAppStrings.current
    val hasImpact = impact != null

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        // Summary row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasImpact) Modifier.clickable { expanded = !expanded } else Modifier)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(s.envImpact, fontSize = 12.sp, color = neutralGray400)
                if (impact == null) {
                    Text("N/A", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = neutralGray400)
                    Text("No impact data", fontSize = 11.sp, color = neutralGray400)
                } else {
                    // Show max impact
                    val maxImpact = maxOf(impact.air, impact.water, impact.soil)
                    val normalizedMax = (maxImpact * 100).coerceIn(0.0, 100.0)
                    val maxColor = getImpactColor(normalizedMax)
                    Text(
                        "%.0f%%".fmt(normalizedMax),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = maxColor,
                    )
                    Text("max impact", fontSize = 11.sp, color = maxColor.copy(alpha = 0.7f))
                }
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            impact == null -> Color(0xFFF5F5F5)
                            maxOf(impact.air, impact.water, impact.soil) < 0.2 -> Color(0xFFE8F5E9)
                            maxOf(impact.air, impact.water, impact.soil) < 0.5 -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    when {
                        impact == null -> "?"
                        maxOf(impact.air, impact.water, impact.soil) < 0.2 -> "✓"
                        maxOf(impact.air, impact.water, impact.soil) < 0.5 -> "⚠"
                        else -> "✗"
                    },
                    fontSize = 24.sp,
                )
            }
        }

        // Expanded detail with bars
        if (expanded && impact != null) {
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                ImpactBar(label = s.airPollution, icon = "💨", value = impact.air)
                ImpactBar(label = s.waterPollution, icon = "💧", value = impact.water)
                ImpactBar(label = s.soilPollution, icon = "🌱", value = impact.soil)
            }
        }
    }
}

@Composable
private fun ImpactBar(label: String, icon: String, value: Double) {
    val normalizedValue = (value * 100).coerceIn(0.0, 100.0)
    val barColor = getImpactColor(normalizedValue)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(icon, fontSize = 14.sp)
                Text(label, fontSize = 12.sp, color = neutralGray700)
            }
            Text(
                "%.1f%%".fmt(normalizedValue),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = barColor,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFEEEEEE))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = (normalizedValue / 100f).toFloat())
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(barColor)
            )
        }
    }
}

// ── Pollutant Breakdown Section ────────────────────────────────────────────────

@Composable
fun PollutantBreakdownSection(
    pollution: Map<String, Double>?,
    modifier: Modifier = Modifier,
) {
    val hasPollution = pollution != null && pollution.isNotEmpty()

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
    ) {
        // Summary row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (hasPollution) Modifier.clickable { expanded = !expanded } else Modifier)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Pollutant Analysis", fontSize = 12.sp, color = neutralGray400)
                if (pollution == null) {
                    Text("N/A", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = neutralGray400)
                    Text("No pollutant analysis", fontSize = 11.sp, color = neutralGray400)
                } else if (pollution.isEmpty()) {
                    Text("Clean", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = pollutantGreen)
                    Text("No pollutants detected", fontSize = 11.sp, color = pollutantGreen)
                } else {
                    Text("${pollution.size}", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = pollutantRed)
                    Text("${pollution.size} pollutants detected", fontSize = 11.sp, color = pollutantRed)
                }
            }
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            pollution == null -> Color(0xFFF5F5F5)
                            pollution.isEmpty() -> Color(0xFFE8F5E9)
                            pollution.size <= 2 -> Color(0xFFFFF3E0)
                            else -> Color(0xFFFFEBEE)
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    when {
                        pollution == null -> "?"
                        pollution.isEmpty() -> "✓"
                        pollution.size <= 2 -> "⚠"
                        else -> "✗"
                    },
                    fontSize = 24.sp,
                )
            }
        }

        // Expanded detail
        if (expanded && hasPollution) {
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Sort by value descending
                val sortedPollutants = pollution.entries
                    .filter { it.value > 0.0 }
                    .sortedByDescending { it.value }

                sortedPollutants.forEach { (key, value) ->
                    val label = com.vodang.greenmind.scandetail.getPollutantLabel(key)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(label, fontSize = 12.sp, color = neutralGray700, modifier = Modifier.weight(1f))
                        Text(
                            "%.2f".fmt(value),
                            fontSize = 12.sp,
                            color = getImpactColor(value * 100),
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

// ── WasteDetectResponse Extension ─────────────────────────────────────────────

val WasteDetectResponse.ecoScore: Int
    get() {
        val avg = (impact.airPollution + impact.waterPollution + impact.soilPollution) / 3.0
        return ((1.0 - avg) * 100).toInt().coerceIn(0, 100)
    }

val WasteDetectResponse.activePollutants: List<Pair<String, Double>>
    get() = pollution.entries
        .filter { it.value > 0.0 }
        .sortedByDescending { it.value }
        .map { it.key to it.value }