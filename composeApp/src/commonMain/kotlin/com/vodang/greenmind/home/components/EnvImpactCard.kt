package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
import kotlin.math.roundToInt


private val red700   = Color(0xFFC62828)
private val red50    = Color(0xFFFFEBEE)
private val orange50 = Color(0xFFFFF3E0)
private val orange600 = Color(0xFFF4511E)
private val amber    = Color(0xFFF57F17)
private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)
private val blue600  = Color(0xFF1976D2)

/** Maximum impact value used to normalise the progress bars. */
private const val IMPACT_MAX = 1.0f

/** Friendly display names for raw pollution keys. */
private val pollutantLabel = mapOf(
    "CO2"              to "CO₂",
    "dioxin"           to "Dioxin",
    "microplastic"     to "Microplastic",
    "toxic_chemicals"  to "Toxic chemicals",
    "non_biodegradable" to "Non-biodegradable",
    "NOx"              to "NOₓ",
    "SO2"              to "SO₂",
    "CH4"              to "CH₄",
    "PM2.5"            to "PM2.5",
    "Pb"               to "Lead (Pb)",
    "Hg"               to "Mercury (Hg)",
    "Cd"               to "Cadmium (Cd)",
    "nitrate"          to "Nitrate",
    "chemical_residue" to "Chemical residue",
    "styrene"          to "Styrene",
)

@Composable
fun EnvImpactCard(result: WasteDetectResponse, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ── Header ────────────────────────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(red50),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("☣️", fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Environmental Impact",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B),
                    )
                    Text(
                        "${result.totalItems} item${if (result.totalItems != 1) "s" else ""} detected · eco score ${result.ecoScore}%",
                        fontSize = 11.sp,
                        color = Color.Gray,
                    )
                }
                // Eco score badge
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(ecoScoreColor(result.ecoScore).copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        "${result.ecoScore}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ecoScoreColor(result.ecoScore),
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // ── Detected items ────────────────────────────────────────────────
            Text(
                "Detected waste",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF616161),
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                result.items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(orange50),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("🗑️", fontSize = 14.sp)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                item.name.replaceFirstChar { it.uppercase() },
                                fontSize = 13.sp,
                                color = Color(0xFF1B1B1B),
                                fontWeight = FontWeight.Medium,
                            )
                            if (item.className.isNotBlank()) {
                                Text(
                                    item.className,
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(red50)
                                .padding(horizontal = 10.dp, vertical = 3.dp),
                        ) {
                            Text(
                                "×${item.quantity}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = red700,
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // ── Impact meters ─────────────────────────────────────────────────
            Text(
                "Pollution impact",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF616161),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ImpactMeter(icon = "💨", label = "Air pollution",   value = result.impact.airPollution.toFloat(),   barColor = red700)
                ImpactMeter(icon = "💧", label = "Water pollution", value = result.impact.waterPollution.toFloat(), barColor = blue600)
                ImpactMeter(icon = "🌱", label = "Soil pollution",  value = result.impact.soilPollution.toFloat(),  barColor = amber)
            }

            // ── Pollutant breakdown ───────────────────────────────────────────
            if (result.pollution.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text(
                    "Pollutant breakdown",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF616161),
                )
                val sortedPollutants = result.pollution.entries
                    .sortedByDescending { it.value }
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sortedPollutants.forEach { (key, value) ->
                        PollutantRow(
                            name  = pollutantLabel[key] ?: key,
                            value = value,
                        )
                    }
                }
            }

            // ── Tip footer ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(green50)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(
                    "♻️  Dispose plastics correctly to reduce these impacts.",
                    fontSize = 11.sp,
                    color = green800,
                    lineHeight = 16.sp,
                )
            }
        }
    }
}

private fun ecoScoreColor(score: Int): Color = when {
    score >= 60 -> Color(0xFF2E7D32)
    score >= 35 -> Color(0xFFE65100)
    else        -> Color(0xFFC62828)
}

@Composable
private fun ImpactMeter(icon: String, label: String, value: Float, barColor: Color) {
    val progress = (value / IMPACT_MAX).coerceIn(0f, 1f)
    val pct = (value * 100).roundToInt()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(icon, fontSize = 14.sp)
                Text(label, fontSize = 12.sp, color = Color(0xFF424242))
            }
            Text(
                "$pct%",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = barColor,
            )
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.12f),
        )
    }
}

@Composable
private fun PollutantRow(name: String, value: Double) {
    val progress = value.toFloat().coerceIn(0f, 1f)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            name,
            fontSize = 11.sp,
            color = Color(0xFF424242),
            modifier = Modifier.width(120.dp),
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(5.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = red700,
            trackColor = red50,
        )
        Text(
            "%.3f".format(value),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = red700,
            modifier = Modifier.width(42.dp),
        )
    }
}
