package com.vodang.greenmind.catalogue

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    val icon: ImageVector,
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
        FeatureEntry(Icons.Filled.Description, s.todos,    s.todosDesc),
        FeatureEntry(Icons.Filled.Analytics,   s.surveys,  s.surveysDesc),
        FeatureEntry(Icons.Filled.Newspaper, s.blog,     s.blogDesc),
        FeatureEntry(Icons.Filled.Public, s.oceanTitle, s.oceanSubtitle),
        FeatureEntry(Icons.Filled.Edit,  s.preAppSurveyTitle, s.preAppSurveySubtitle, onClick = onPreAppSurvey),
    )
    val householdFeatures = listOf(
        FeatureEntry(Icons.Filled.Refresh,  s.wasteSort,              s.wasteSortDesc),
        FeatureEntry(Icons.Filled.Delete, s.wasteReport,            s.wasteReportDesc, onClick = onWasteReport),
        FeatureEntry(Icons.Filled.Scale, s.wasteTotalMassTitle,     s.wasteTotalMassDesc),
        FeatureEntry(Icons.Filled.Warning, s.environmentalImpact,     s.environmentalImpactDesc),
        FeatureEntry(Icons.Filled.Analytics, s.wasteImpactTitle,        s.wasteImpactDesc),
        FeatureEntry(Icons.Filled.TrendingUp, s.wasteStatTitle,          s.wasteStatDesc),
        FeatureEntry(Icons.Filled.Description, s.householdWasteStatusTitle, s.householdWasteStatusDesc),
        FeatureEntry(Icons.Filled.Lightbulb, s.electricityUsage,         s.electricityChartDesc),
        FeatureEntry(Icons.Filled.DirectionsWalk, s.walkDistance,            s.walkValue),
        FeatureEntry(Icons.Filled.Restaurant, s.scanMeal,               s.scanMealDesc),
        FeatureEntry(Icons.Filled.Receipt, s.scanBill,               s.scanBillDesc),
        FeatureEntry(Icons.Filled.Newspaper, s.blog,                    s.blogDesc),
        FeatureEntry(Icons.Filled.Handshake, s.campaignsTitle,         s.campaignsDesc),
    )
    val collectorFeatures = listOf(
        FeatureEntry(Icons.Filled.Map, s.heatmapFeatureLabel,  s.heatmapFeatureDesc),
        FeatureEntry(Icons.Filled.LocationOn, s.routeLabel,            s.scheduleDesc),
        FeatureEntry(Icons.Filled.CheckCircle, s.checkInCardTitle,      s.checkInButton),
    )
    val volunteerFeatures = listOf(
        FeatureEntry(Icons.Filled.Handshake, s.volunteerTitle,        s.volunteerSubtitle),
        FeatureEntry(Icons.Filled.Event, s.volunteerEventsCardTitle, s.volunteerUpcomingTitle),
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
                roleIcon = Icons.Filled.Group,
                roleLabel = s.allUsersLabel,
                roleColor = Color(0xFF1565C0),
                roleBg = Color(0xFFE3F2FD),
                features = allFeatures
            )
            CatalogueSection(
                roleIcon = Icons.Filled.House,
                roleLabel = s.householdRole,
                roleColor = green800,
                roleBg = green50,
                features = householdFeatures
            )
            CatalogueSection(
                roleIcon = Icons.Filled.LocalShipping,
                roleLabel = s.collectorRole,
                roleColor = Color(0xFFE65100),
                roleBg = Color(0xFFFFF3E0),
                features = collectorFeatures
            )
            CatalogueSection(
                roleIcon = Icons.Filled.Handshake,
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
    roleIcon: ImageVector,
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
                Icon(
                    imageVector = roleIcon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = roleColor,
                )
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
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = bgColor,
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(feature.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B1B1B))
                Spacer(Modifier.height(2.dp))
                Text(feature.desc, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            }
        }
    }
}
