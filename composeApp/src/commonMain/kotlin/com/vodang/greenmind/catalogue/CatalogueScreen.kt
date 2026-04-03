package com.vodang.greenmind.catalogue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

// ── Feature catalogue ──────────────────────────────────────────────────────
//
// HOW TO ADD A NEW FEATURE:
//   1. Add a new FeatureEntry to the appropriate section list below.
//   2. Use the role that best describes who can use it (ALL / HOUSEHOLD /
//      COLLECTOR / VOLUNTEER).
//   3. The description should be one short sentence — what the feature does.
//
// This screen is the single source of truth for what the app can do.
// Keep it up to date whenever a feature is added, changed, or removed.
// ──────────────────────────────────────────────────────────────────────────

private data class FeatureEntry(
    val icon: String,
    val title: String,
    val desc: String,
    val onClick: (() -> Unit)? = null,
)

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

@Composable
fun CatalogueScreen(onWasteReport: () -> Unit, onPreAppSurvey: () -> Unit = {}) {
    val s = LocalAppStrings.current

    // ── Feature lists ──────────────────────────────────────────────────────
    val allFeatures = listOf(
        FeatureEntry("📋", s.todos,    s.todosDesc),
        FeatureEntry("📊", s.surveys,  s.surveysDesc),
        FeatureEntry("📰", s.blog,     s.blogDesc),
        FeatureEntry("🌊", s.oceanTitle, s.oceanSubtitle),
        FeatureEntry("📝", s.preAppSurveyTitle, s.preAppSurveySubtitle, onClick = onPreAppSurvey),
    )
    val householdFeatures = listOf(
        FeatureEntry("♻️", s.wasteSort,       s.wasteSortDesc),
        FeatureEntry("🗑️", s.garbageDrop,     s.garbageDropDesc),
        FeatureEntry("�", s.wasteReport,    s.wasteReportDesc, onClick = onWasteReport),
        FeatureEntry("�💡", s.electricityUsage, s.electricityChartDesc),
        FeatureEntry("🍽️", s.scanMeal,        s.scanMealDesc),
        FeatureEntry("🧾", s.scanBill,         s.scanBillDesc),
        FeatureEntry("🚶", s.walkDistance,     s.walkValue),
        FeatureEntry("☣️", "Environmental Impact", "Shows CO₂, microplastic, and pollution scores from waste scans"),
    )
    val collectorFeatures = listOf(
        FeatureEntry("🗺️", s.heatmapFeatureLabel,  s.heatmapFeatureDesc),
        FeatureEntry("📍", s.routeLabel,            s.scheduleDesc),
        FeatureEntry("✅", s.checkInCardTitle,      s.checkInButton),
    )
    val volunteerFeatures = listOf(
        FeatureEntry("🤝", s.volunteerTitle,        s.volunteerSubtitle),
        FeatureEntry("🗓️", s.volunteerEventsCardTitle, s.volunteerUpcomingTitle),
    )
    // ──────────────────────────────────────────────────────────────────────

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
            CatalogueSection(
                roleIcon = "👥",
                roleLabel = "All users",
                roleColor = Color(0xFF1565C0),
                roleBg = Color(0xFFE3F2FD),
                features = allFeatures
            )
            CatalogueSection(
                roleIcon = "🏠",
                roleLabel = s.householdRole,
                roleColor = green800,
                roleBg = green50,
                features = householdFeatures
            )
            CatalogueSection(
                roleIcon = "🚛",
                roleLabel = s.collectorRole,
                roleColor = Color(0xFFE65100),
                roleBg = Color(0xFFFFF3E0),
                features = collectorFeatures
            )
            CatalogueSection(
                roleIcon = "🤝",
                roleLabel = s.volunteerRole,
                roleColor = Color(0xFF6A1B9A),
                roleBg = Color(0xFFF3E5F5),
                features = volunteerFeatures
            )
            Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun CatalogueSection(
    roleIcon: String,
    roleLabel: String,
    roleColor: Color,
    roleBg: Color,
    features: List<FeatureEntry>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(roleBg),
                contentAlignment = Alignment.Center
            ) {
                Text(roleIcon, fontSize = 16.sp)
            }
            Text(
                text = roleLabel,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = roleColor
            )
        }

        // Feature cards
        features.forEach { feature ->
            CatalogueCard(feature = feature, accentColor = roleColor, bgColor = roleBg)
        }
    }
}

@Composable
private fun CatalogueCard(feature: FeatureEntry, accentColor: Color, bgColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (feature.onClick != null) Modifier.clickable { feature.onClick.invoke() } else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Text(feature.icon, fontSize = 22.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(feature.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B1B1B))
                Spacer(Modifier.height(2.dp))
                Text(feature.desc, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            }
        }
    }
}
