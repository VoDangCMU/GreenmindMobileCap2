package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.UserDto
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800v = Color(0xFF2E7D32)
private val green600v = Color(0xFF388E3C)
private val green50v  = Color(0xFFE8F5E9)
private val blue600v  = Color(0xFF1976D2)
private val blue50v   = Color(0xFFE3F2FD)
private val teal600v  = Color(0xFF00897B)
private val teal50v   = Color(0xFFE0F2F1)

data class VolunteerEvent(
    val id: Int,
    val title: String,
    val location: String,
    val date: String,
    val participants: Int,
    val isRegistered: Boolean,
    val isActive: Boolean,
)

@Composable
fun VolunteerDashboard(user: UserDto? = null) {
    val s = LocalAppStrings.current

    val activeEvents = listOf(
        VolunteerEvent(1, "Dọn rác bãi biển Mỹ Khê", "Bãi biển Mỹ Khê, Sơn Trà", "19/03/2026 · 07:00", 34, true, true),
        VolunteerEvent(2, "Trồng cây xanh Hải Châu", "Công viên 29/3, Hải Châu", "19/03/2026 · 14:00", 18, false, true),
    )
    val upcomingEvents = listOf(
        VolunteerEvent(3, "Dọn vệ sinh kênh Phú Lộc", "Kênh Phú Lộc, Thanh Khê", "22/03/2026 · 06:30", 45, true, false),
        VolunteerEvent(4, "Thu gom rác thải điện tử", "UBND Q. Ngũ Hành Sơn", "25/03/2026 · 08:00", 22, false, false),
        VolunteerEvent(5, "Vệ sinh cống thoát nước", "Khu dân cư Liên Chiểu", "28/03/2026 · 07:30", 30, false, false),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${user?.fullName ?: s.volunteerTitle} 🤝",
                    fontSize = 20.sp, fontWeight = FontWeight.Bold, color = green800v
                )
                Text(s.volunteerSubtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier.size(56.dp).background(green800v, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text("🤝", fontSize = 24.sp)
            }
        }

        // Metrics
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard("⏱️", s.volunteerHoursLabel, s.volunteerHoursValue, "", teal50v, teal600v, Modifier.weight(1f).aspectRatio(1f))
            MetricCard("🗓️", s.volunteerEventsLabel, s.volunteerEventsValue, "", green50v, green800v, Modifier.weight(1f).aspectRatio(1f))
            MetricCard("⭐", s.volunteerPointsLabel, s.volunteerPointsValue, "", blue50v, blue600v, Modifier.weight(1f).aspectRatio(1f))
        }

        // Heatmap
        GarbageHeatmapCard()

        // Active events
        SectionCard {
            Text(s.volunteerEventsCardTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                activeEvents.forEach { event -> VolunteerEventRow(event, s.volunteerJoinButton, s.volunteerRegistered) }
            }
        }

        // Upcoming events
        SectionCard {
            Text(s.volunteerUpcomingTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                upcomingEvents.forEach { event -> VolunteerEventRow(event, s.volunteerJoinButton, s.volunteerRegistered) }
            }
        }
    }
}

@Composable
private fun VolunteerEventRow(event: VolunteerEvent, joinLabel: String, registeredLabel: String) {
    var registered by remember(event.id) { mutableStateOf(event.isRegistered) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (event.isActive) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Text(if (event.isActive) "🟢" else "📅", fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(event.title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
            Spacer(Modifier.height(2.dp))
            Text("📍 ${event.location}", fontSize = 11.sp, color = Color.Gray)
            Text("🕐 ${event.date}  ·  👥 ${event.participants}", fontSize = 11.sp, color = Color.Gray)
        }
        Spacer(Modifier.width(8.dp))
        if (registered) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE8F5E9))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("✓ $registeredLabel", fontSize = 11.sp, color = green600v, fontWeight = FontWeight.Medium)
            }
        } else {
            Button(
                onClick = { registered = true },
                colors = ButtonDefaults.buttonColors(containerColor = green800v),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Text(joinLabel, fontSize = 11.sp, color = Color.White)
            }
        }
    }
}
