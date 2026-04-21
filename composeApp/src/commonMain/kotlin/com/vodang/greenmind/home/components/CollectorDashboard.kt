package com.vodang.greenmind.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.LinearProgressIndicator
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
import com.vodang.greenmind.api.auth.UserDto
import com.vodang.greenmind.api.wastecollector.CollectorDetectRecordDto
import com.vodang.greenmind.api.wastecollector.getBroughtOut
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.launch

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50 = Color(0xFFE8F5E9)
private val amber600 = Color(0xFFFFB300)
private val blue600 = Color(0xFF1976D2)
private val blue50 = Color(0xFFE3F2FD)

data class WastePoint(val id: Int, val reportId: String, val address: String, val zone: String, val collected: Boolean, val bags: Int, val lat: Double?, val lng: Double?)

private fun CollectorDetectRecordDto.toWastePoint(index: Int, unknownLocation: String) = WastePoint(
    id = index,
    reportId = id,
    address = household?.address ?: unknownLocation,
    zone = detectType,
    collected = status == "picked_up" || status == "done" || status == "completed" || status == "resolved",
    bags = totalObjects ?: totalMassKg?.toInt() ?: 0,
    lat = household?.lat?.toDoubleOrNull(),
    lng = household?.lng?.toDoubleOrNull(),
)

@Composable
fun CollectorDashboard(user: UserDto? = null, scrollState: ScrollState = rememberScrollState()) {
    val s = LocalAppStrings.current

    val scope = rememberCoroutineScope()
    var reports by remember { mutableStateOf<List<CollectorDetectRecordDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var checkInReportId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken() ?: run { isLoading = false; return@LaunchedEffect }
        try {
            reports = getBroughtOut(token).data
            errorMsg = null
        } catch (e: Throwable) {
            errorMsg = e.message
        } finally {
            isLoading = false
        }
    }

    val points = reports.mapIndexed { i, dto -> dto.toWastePoint(i, s.unknownLocation) }
    val routePoints = reports.mapNotNull {
        val lat = it.household?.lat?.toDoubleOrNull()
        val lng = it.household?.lng?.toDoubleOrNull()
        if (lat != null && lng != null) {
            RouteMapPoint(lat = lat, lng = lng, label = it.detectedBy?.fullName ?: "User")
        } else null
    }
    val collectedCount = points.count { it.collected }
    val totalCount = points.size

    val token = SettingsStore.getAccessToken() ?: ""
    // Always composed so the activity-result launcher stays registered.
    CheckInScanFlow(
        reportId = checkInReportId,
        accessToken = token,
        onSuccess = {
            checkInReportId = null
            scope.launch {
                runCatching { 
                    reports = getBroughtOut(token).data
                }
            }
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
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = amber600)
                }
            }
            errorMsg != null -> {
                SectionCard {
                    Text(
                        s.errorDisplay(errorMsg ?: ""),
                        fontSize = 13.sp,
                        color = Color(0xFFC62828),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                    )
                }
            }
            else -> {
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
                    MetricCard(Icons.Filled.Map, s.zoneLabel, s.zoneValue, s.today, Color(0xFFFFF8E1), amber600, amber600, Modifier.weight(1f).aspectRatio(1f))
                    MetricCard(Icons.Filled.Scale, s.bagsLabel, "${points.sumOf { it.bags }} kg", s.bagsEstimated, green50, green800, green800, Modifier.weight(1f).aspectRatio(1f))
                    MetricCard(Icons.Filled.Assignment, s.routeLabel, "$totalCount", s.today, blue50, blue600, blue600, Modifier.weight(1f).aspectRatio(1f))
                }

                CheckInCard(points, onCheckInClick = { reportId -> checkInReportId = reportId })
                CollectionRouteCard(points)
                CollectionRouteMapCard(points = routePoints)
                // GarbageHeatmapCard()
            }
        }
    }
}
