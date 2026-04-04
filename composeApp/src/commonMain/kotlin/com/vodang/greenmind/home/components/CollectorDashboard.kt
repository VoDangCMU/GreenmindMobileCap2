package com.vodang.greenmind.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.UserDto
import com.vodang.greenmind.api.wastecollector.WasteCollectorReportDto
import com.vodang.greenmind.api.wastecollector.getAssignedReports
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.HouseholdWasteStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50 = Color(0xFFE8F5E9)
private val amber600 = Color(0xFFFFB300)
private val blue600 = Color(0xFF1976D2)
private val blue50 = Color(0xFFE3F2FD)

data class WastePoint(val id: Int, val reportId: String, val address: String, val zone: String, val collected: Boolean, val bags: Int)

private fun WasteCollectorReportDto.toWastePoint(index: Int) = WastePoint(
    id        = index,
    reportId  = id,
    address   = "%.4f, %.4f".format(lat, lng),
    zone      = wasteType,
    collected = status == "resolved" || status == "completed",
    bags      = wasteKg.toInt(),
)

@Composable
fun CollectorDashboard(user: UserDto? = null, scrollState: ScrollState = rememberScrollState()) {
    val s = LocalAppStrings.current
    var reports by remember { mutableStateOf<List<WasteCollectorReportDto>>(emptyList()) }
    var checkInReportId by remember { mutableStateOf<String?>(null) }
    var accessToken by remember { mutableStateOf("") }

    // Load token once and fetch assigned reports
    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect
        accessToken = token
        try {
            reports = getAssignedReports(token).data
        } catch (e: Throwable) {
            AppLogger.e("CollectorDashboard", "getAssignedReports failed: ${e.message}")
        }
    }

    val points = reports.mapIndexed { i, r -> r.toWastePoint(i) }
    val routePoints = reports.map { RouteMapPoint(lat = it.lat, lng = it.lng, label = it.code) }
    val collectedCount = points.count { it.collected }
    val totalCount = points.size

    // Must stay in composition so activity-result launcher stays registered
    CheckInScanFlow(
        reportId    = checkInReportId,
        accessToken = accessToken,
        onSuccess   = {
            val id = checkInReportId
            checkInReportId = null
            if (id != null) {
                reports = reports.map { if (it.id == id) it.copy(status = "resolved") else it }
            }
            HouseholdWasteStore.markAllProcessed()
        },
        onDismiss = { checkInReportId = null },
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionCard {
            Text(s.progressToday, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { if (totalCount == 0) 0f else collectedCount.toFloat() / totalCount },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(6.dp)),
                color = amber600,
                trackColor = Color(0xFFFFF8E1),
            )
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(s.pointsCollected(collectedCount), fontSize = 12.sp, color = green600, fontWeight = FontWeight.Medium)
                Text(s.pointsRemaining(totalCount - collectedCount), fontSize = 12.sp, color = Color(0xFFE65100))
            }
        }

        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("🗺️", s.zoneLabel, s.zoneValue, s.today, Color(0xFFFFF8E1), amber600, Modifier.weight(1f).aspectRatio(1f))
            MetricCard("⚖️", s.bagsLabel, "${points.sumOf { it.bags }} kg", s.bagsEstimated, green50, green800, Modifier.weight(1f).aspectRatio(1f))
            MetricCard("📋", s.routeLabel, "$totalCount", s.today, blue50, blue600, Modifier.weight(1f).aspectRatio(1f))
        }

        CheckInCard(
            points = points,
            onCheckInClick = { reportId -> checkInReportId = reportId },
        )
        CollectionRouteCard(points)
        CollectionRouteMapCard(points = routePoints)

        Text(s.features, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton("🗺️", s.heatmapFeatureLabel, s.heatmapFeatureDesc, Color(0xFFFFF8E1), amber600, Modifier.weight(1f)) { }
                FeatureButton("📅", s.scheduleLabel, s.scheduleDesc, blue50, blue600, Modifier.weight(1f)) { }
            }
        }
    }
}
