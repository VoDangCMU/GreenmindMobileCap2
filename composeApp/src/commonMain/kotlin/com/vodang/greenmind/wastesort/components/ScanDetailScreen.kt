package com.vodang.greenmind.wastesort.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.store.HouseholdWasteStore
import com.vodang.greenmind.wastereport.NetworkImage
import com.vodang.greenmind.wastesort.WasteSortEntry
import com.vodang.greenmind.wastesort.categoryBg
import com.vodang.greenmind.wastesort.categoryColor
import com.vodang.greenmind.wastesort.categoryEmoji
import com.vodang.greenmind.wastesort.categoryLabel
import com.vodang.greenmind.wastesort.green50
import com.vodang.greenmind.wastesort.green800

@Composable
fun ScanDetailScreen(entry: WasteSortEntry, onBack: () -> Unit) {
    val categories = entry.grouped.keys.toList()
    var selectedCategory by remember(entry.id) {
        mutableStateOf(categories.firstOrNull() ?: "")
    }

    val records by HouseholdWasteStore.records.collectAsState()
    val record = records.find { it.id == entry.id }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
        ) {
            // Detected image with back button overlay
            item {
                Box {
                    NetworkImage(
                        url = entry.imageUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight(),
                    )
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.35f))
                            .clickable(onClick = onBack),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text("←", fontSize = 18.sp, color = Color.White)
                    }
                }
            }

            // Category tabs + segment grid
            if (entry.grouped.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            "By Category",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B1B1B),
                        )

                        // Category tab pills
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            categories.forEach { cat ->
                                val count    = entry.grouped[cat]?.size ?: 0
                                val selected = cat == selectedCategory
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (selected) categoryColor(cat) else categoryBg(cat)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(categoryEmoji(cat), fontSize = 14.sp)
                                        Text(
                                            "${categoryLabel(cat)} ($count)",
                                            fontSize = 13.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) Color.White else categoryColor(cat),
                                        )
                                    }
                                }
                            }
                        }

                        // Segment image grid
                        val imageUrls = entry.grouped[selectedCategory] ?: emptyList()
                        SegmentGrid(imageUrls = imageUrls, category = selectedCategory)
                    }
                }
            }

            if (record != null) {
                item {
                    WasteStatusActionButton(
                        status = record.status,
                        onAction = { next -> HouseholdWasteStore.updateStatus(entry.id, next) },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .navigationBarsPadding(),
                    )
                }
            }
        }
    }
}

@Composable
fun WasteStatusActionButton(
    status: String,
    onAction: (nextStatus: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (status) {
        "SCANNED_RAW" -> Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFF1565C0),
            modifier = modifier.fillMaxWidth(),
            onClick = { onAction("SCANNED_SORTED") },
        ) {
            Text(
                "✅  Mark as Sorted",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
        "SCANNED_SORTED" -> Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF59E0B),
            modifier = modifier.fillMaxWidth(),
            onClick = { onAction("DISPOSED") },
        ) {
            Text(
                "🗑️  Dispose",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp),
            )
        }
    }
}
