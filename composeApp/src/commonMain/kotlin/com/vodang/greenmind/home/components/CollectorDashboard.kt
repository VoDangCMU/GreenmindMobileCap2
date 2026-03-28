package com.vodang.greenmind.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.UserDto
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50 = Color(0xFFE8F5E9)
private val amber600 = Color(0xFFFFB300)
private val blue600 = Color(0xFF1976D2)
private val blue50 = Color(0xFFE3F2FD)

data class WastePoint(val id: Int, val address: String, val zone: String, val collected: Boolean, val bags: Int)

@Composable
fun CollectorDashboard(user: UserDto? = null, scrollState: ScrollState = rememberScrollState()) {
    val s = LocalAppStrings.current
    // TODO: Replace with live collection route data from the API.
    //       Expected source: GET /collector/route  (returns assigned WastePoints for today's shift)
    //       WastePoint should eventually come from a RouteStore/CollectorStore that fetches on login.
    val points = listOf(
        WastePoint(1, "12 Trần Phú, Hải Châu",         "Khu A", true,  3),
        WastePoint(2, "45 Lê Duẩn, Hải Châu",          "Khu A", true,  5),
        WastePoint(3, "7 Điện Biên Phủ, Thanh Khê",    "Khu B", false, 2),
        WastePoint(4, "88 Hùng Vương, Thanh Khê",      "Khu B", false, 4),
        WastePoint(5, "23 Phạm Văn Đồng, Sơn Trà",    "Khu C", false, 6),
        WastePoint(6, "101 Hoàng Sa, Sơn Trà",         "Khu C", true,  2),
    )
    val collectedCount = points.count { it.collected }
    val totalCount = points.size

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Row(verticalAlignment = Alignment.CenterVertically) {
        //     // Column(modifier = Modifier.weight(1f)) {
        //     //     Text(
        //     //         "${user?.fullName ?: s.collectorTitle} 🚛",
        //     //         fontSize = 20.sp, fontWeight = FontWeight.Bold, color = green800
        //     //     )
        //     //     Text(s.collectorShift, fontSize = 12.sp, color = Color.Gray)
        //     // }
        //     Box(modifier = Modifier.size(56.dp).background(amber600, CircleShape), contentAlignment = Alignment.Center) {
        //         Column(horizontalAlignment = Alignment.CenterHorizontally) {
        //             Text("$collectedCount/$totalCount", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
        //             Text(s.collectorPointsUnit, fontSize = 9.sp, color = Color.White.copy(alpha = 0.85f))
        //         }
        //     }
        // }

        SectionCard {
            Text(s.progressToday, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { collectedCount.toFloat() / totalCount },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)),
                color = amber600,
                trackColor = Color(0xFFFFF8E1)
            )
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(s.pointsCollected(collectedCount), fontSize = 12.sp, color = green600, fontWeight = FontWeight.Medium)
                Text(s.pointsRemaining(totalCount - collectedCount), fontSize = 12.sp, color = Color(0xFFE65100))
            }
        }

        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("🗺️", s.zoneLabel, s.zoneValue, s.today, Color(0xFFFFF8E1), amber600, Modifier.weight(1f).aspectRatio(1f))
            MetricCard("🛍️", s.bagsLabel, "${points.sumOf { it.bags }} ${s.bagsUnit}", s.bagsEstimated, green50, green800, Modifier.weight(1f).aspectRatio(1f))
            // TODO: Replace "8.2 km" with real route distance calculated from the assigned WastePoints.
            MetricCard("📍", s.routeLabel, "8.2 km", s.today, blue50, blue600, Modifier.weight(1f).aspectRatio(1f))
        }

        
        CheckInCard(points)
        CollectionRouteCard(points)
        GarbageHeatmapCard()
        Text(s.features, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // TODO: Navigate to full-screen garbage heatmap view.
                FeatureButton("🗺️", s.heatmapFeatureLabel, s.heatmapFeatureDesc, Color(0xFFFFF8E1), amber600, Modifier.weight(1f)) { }
                // TODO: Navigate to collection schedule / shift calendar screen.
                FeatureButton("📅", s.scheduleLabel, s.scheduleDesc, blue50, blue600, Modifier.weight(1f)) { }
            }
        }
    }
}
