package com.vodang.greenmind.householdwaste

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.wastereport.NetworkImage

// ── Status catalogue ──────────────────────────────────────────────────────────

private data class WasteStatus(val key: String, val label: String, val color: Color)

private val wasteStatuses = listOf(
    WasteStatus("SCANNED_RAW",    "Scanned",  Color(0xFF9CA3AF)),
    WasteStatus("SCANNED_SORTED", "Sorted",   Color(0xFF3B82F6)),
    WasteStatus("DISPOSED",       "Disposed", Color(0xFFF59E0B)),
    WasteStatus("PENDING",        "Pending",  Color(0xFFEAB308)),
    WasteStatus("PROCESSED",      "Done",     Color(0xFF10B981)),
    WasteStatus("ERROR",          "Error",    Color(0xFFEF4444)),
    WasteStatus("SKIPPED",        "Skipped",  Color(0xFF6B7280)),
)

private fun statusFor(key: String) =
    wasteStatuses.find { it.key == key } ?: WasteStatus(key, key, Color(0xFF9CA3AF))

private val pipeline = listOf("SCANNED_RAW", "SCANNED_SORTED", "DISPOSED", "PROCESSED")

// ── Mock record ───────────────────────────────────────────────────────────────

private data class WasteScanRecord(
    val id: String,
    val totalObjects: Int,
    val imageUrl: String,
    val segments: List<String>,
    val wasteCategory: String,
    val status: String,
    val scannedAt: String,
    val co2AvoidedKg: Double,
)

private val mockRecord = WasteScanRecord(
    id           = "WSR-20260401-001",
    totalObjects = 20,
    imageUrl     = "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229811/yolo_detect/detect/cc68420fb9d244aa8fbc8d04f9c36b3e.jpg",
    segments     = listOf(
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229793/yolo_segments/segments/5a773619de2f456d8f1125414e674932.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229794/yolo_segments/segments/1c9467c3460a4c8384e62e1a2fc89c44.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229794/yolo_segments/segments/784f03c4f3ea4ef398759eb6f7c74557.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229795/yolo_segments/segments/46fc23d93e0845e4a39711312ffbd382.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229796/yolo_segments/segments/6cfd851246704eee9d2879692e5831ce.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229797/yolo_segments/segments/40dc1771ad3148c8a546573d8809b48b.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229798/yolo_segments/segments/fda0c521ac864860a57ffe6280aeef67.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229799/yolo_segments/segments/f1c3bdbeebc24381b741b73815386d66.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229799/yolo_segments/segments/be5bcce1b09343aea2a26c74d7e65ee5.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229800/yolo_segments/segments/f18643d656564409a03c2f959fcb1901.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229801/yolo_segments/segments/cbce16b89a8042d4b1824af23b240f84.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229802/yolo_segments/segments/899fdbd04b9a4292b07e313759552488.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229803/yolo_segments/segments/f3fe12a5832b4489bc7d2c8c33f2cd75.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229804/yolo_segments/segments/f97951adf882430c830e10a8fd976da3.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229805/yolo_segments/segments/53376faf83244bb0a40823bdc7dd5fb1.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229806/yolo_segments/segments/98f0a5c943774825aed021a9ff197e78.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229807/yolo_segments/segments/ea2b92dce5a644f1976786d50d2b34d1.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229808/yolo_segments/segments/326544ca391f4cbcbc531a6aee7ea8e3.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229809/yolo_segments/segments/4a953c3efe0c4f4ca80d801b17c1f9b5.png",
        "https://res.cloudinary.com/dc8q7sv1f/image/upload/v1775229805/yolo_segments/segments/2dc9dffad09c4d149b60bd449dce3498.png"
    ),
    wasteCategory = "Recyclable",
    status        = "SCANNED_SORTED",
    scannedAt     = "2026-04-01  09:14",
    co2AvoidedKg  = 0.76,
)

// ── Palette ───────────────────────────────────────────────────────────────────

private val purple800 = Color(0xFF6A1B9A)
private val green700  = Color(0xFF388E3C)
private val green50   = Color(0xFFE8F5E9)
private val bgGray    = Color(0xFFF5F5F5)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun HouseholdWasteScreen() {
    Box(modifier = Modifier.fillMaxSize().background(bgGray)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("🗑️  Household Waste", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = purple800)
                    Text("Track your scanned waste items", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Recent Scans", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                WasteScanCard(mockRecord)
                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

// ── Card ──────────────────────────────────────────────────────────────────────

@Composable
private fun WasteScanCard(record: WasteScanRecord) {
    val status = statusFor(record.status)
    val pipelineIndex = pipeline.indexOf(record.status)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column {
            // ── Main scan image ───────────────────────────────────────────────
            Box {
                NetworkImage(
                    url = record.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text("♻️  ${record.totalObjects} objects", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF3B82F6).copy(alpha = 0.88f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Text(record.wasteCategory, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // ── Header ────────────────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Waste Scan", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                        Text(record.scannedAt, fontSize = 11.sp, color = Color.Gray)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(status.color.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                    ) {
                        Text(status.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = status.color)
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // ── Segment thumbnails ────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Detected items  (${record.segments.size})",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF424242),
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(record.segments) { url ->
                            NetworkImage(
                                url = url,
                                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // ── Pipeline progress ─────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Status", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        pipeline.forEachIndexed { i, key ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(if (i <= pipelineIndex) statusFor(key).color else Color(0xFFE5E7EB))
                            )
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        pipeline.forEachIndexed { i, key ->
                            Text(
                                statusFor(key).label,
                                fontSize = 9.sp,
                                color = if (i <= pipelineIndex) Color(0xFF374151) else Color(0xFFD1D5DB),
                                fontWeight = if (i == pipelineIndex) FontWeight.Bold else FontWeight.Normal,
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // ── Waste impact ──────────────────────────────────────────────
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Waste Impact", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color(0xFF424242))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ImpactChip("♻️", "${record.totalObjects}", "Recyclable", Color(0xFFEFF6FF), Color(0xFF3B82F6), Modifier.weight(1f))
                        ImpactChip("💨", "${record.co2AvoidedKg} kg", "CO₂ avoided", green50, green700, Modifier.weight(1f))
                        ImpactChip("🌱", "Low", "Pollution risk", green50, green700, Modifier.weight(1f))
                    }
                }

                Text(record.id, fontSize = 10.sp, color = Color(0xFFBDBDBD))
            }
        }
    }
}

@Composable
private fun ImpactChip(emoji: String, value: String, label: String, bg: Color, fg: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(emoji, fontSize = 18.sp)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fg)
        Text(label, fontSize = 9.sp, color = fg.copy(alpha = 0.75f))
    }
}
