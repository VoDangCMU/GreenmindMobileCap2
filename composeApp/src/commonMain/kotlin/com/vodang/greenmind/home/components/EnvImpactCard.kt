package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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

// ── EnvImpactCard ─────────────────────────────────────────────────────────────
//
// Displays the environmental pollution impact from a plastic-waste scan result.
// Currently renders mock data matching the API response shape.
//
// TODO: Replace mock data with real DTO from the YOLO-detect API endpoint.
//       API response shape (see api/wastereport/WasteReportApi.kt or similar):
//         items          : List<DetectedItem>  (name, quantity, area)
//         total_objects  : Int
//         image_url      : String
//         pollution      : Map<String, Double>  (CO2, microplastic, dioxin, …)
//         impact         : ImpactSummary        (air_pollution, water_pollution, soil_pollution)
//
// TODO: Pass the scan result as a parameter once the API model is wired in.
//       Suggested signature:
//         fun EnvImpactCard(result: YoloScanResult, modifier: Modifier = Modifier)
//
// ─────────────────────────────────────────────────────────────────────────────

// ── Mock data ─────────────────────────────────────────────────────────────────
// TODO: Remove once real data flows in from the API.
private data class MockDetectedItem(val name: String, val quantity: Int, val area: Int)

private val mockItems = listOf(
    MockDetectedItem("Plastic film", 9, 147757),
    MockDetectedItem("Single-use carrier bag", 2, 18706),
)
private val mockTotalObjects = 11

// Impact values from the API's `impact` object — range roughly 0..2+
// TODO: Replace with real ImpactSummary fields from the API DTO.
private val mockAirPollution    = 1.23f   // air_pollution
private val mockWaterPollution  = 0.69f   // water_pollution
private val mockSoilPollution   = 1.39f   // soil_pollution

// Active pollutants (non-zero values) from the API's `pollution` object.
// TODO: Filter these dynamically from the real pollution map.
private val mockActivePollutants = listOf("CO₂", "Microplastic", "Dioxin", "Non-biodegradable")
// ─────────────────────────────────────────────────────────────────────────────

private val red700   = Color(0xFFC62828)
private val red50    = Color(0xFFFFEBEE)
private val orange50 = Color(0xFFFFF3E0)
private val amber    = Color(0xFFF57F17)
private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

/** Maximum impact value used to normalise the progress bars. */
private const val IMPACT_MAX = 2.0f

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnvImpactCard(modifier: Modifier = Modifier) {
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
                Column {
                    Text(
                        "Environmental Impact",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B1B1B),
                    )
                    Text(
                        // TODO: Replace with last-scan timestamp from the DTO.
                        "Last scan · $mockTotalObjects objects detected",
                        fontSize = 11.sp,
                        color = Color.Gray,
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
            // TODO: Replace mockItems with real items list from the DTO.
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                mockItems.forEach { item ->
                    DetectedItemRow(item)
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
            // TODO: Replace mock values with real impact fields from the DTO.
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ImpactMeter(
                    icon = "💨",
                    label = "Air pollution",
                    value = mockAirPollution,
                    max = IMPACT_MAX,
                    barColor = red700,
                )
                ImpactMeter(
                    icon = "💧",
                    label = "Water pollution",
                    value = mockWaterPollution,
                    max = IMPACT_MAX,
                    barColor = Color(0xFF1565C0),
                )
                ImpactMeter(
                    icon = "🌱",
                    label = "Soil pollution",
                    value = mockSoilPollution,
                    max = IMPACT_MAX,
                    barColor = amber,
                )
            }

            HorizontalDivider(color = Color(0xFFEEEEEE))

            // ── Active pollutant chips ────────────────────────────────────────
            Text(
                "Active pollutants",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF616161),
            )
            // TODO: Filter from real pollution map — only show keys with value > 0.
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                mockActivePollutants.forEach { name ->
                    PollutantChip(name)
                }
            }

            // ── Tip footer ────────────────────────────────────────────────────
            // TODO: Surface a tip string from i18n / LocalAppStrings once added.
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

@Composable
private fun DetectedItemRow(item: MockDetectedItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
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
            Text(item.name, fontSize = 13.sp, color = Color(0xFF1B1B1B))
        }
        // Quantity badge
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

@Composable
private fun ImpactMeter(
    icon: String,
    label: String,
    value: Float,
    max: Float,
    barColor: Color,
) {
    val progress = (value / max).coerceIn(0f, 1f)
    val whole = value.toInt()
    val frac = ((value - whole) * 100).toInt()
    val displayValue = "$whole.${frac.toString().padStart(2, '0')}"

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
                displayValue,
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
private fun PollutantChip(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(red50)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            name,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = red700,
        )
    }
}
