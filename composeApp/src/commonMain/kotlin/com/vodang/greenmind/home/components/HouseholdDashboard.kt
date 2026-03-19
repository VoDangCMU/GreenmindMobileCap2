package com.vodang.greenmind.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800h = Color(0xFF2E7D32)
private val green600h = Color(0xFF388E3C)
private val green50h  = Color(0xFFE8F5E9)
private val blue600h  = Color(0xFF1976D2)
private val blue50h   = Color(0xFFE3F2FD)
private val orange50h = Color(0xFFFFF3E0)

@Composable
fun HouseholdDashboard() {
    val s = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OceanScoreCard(ocean = 73)

        Text(s.todayOverview, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("♻️", s.wasteSort, s.wasteSortValue, s.today, green50h, green800h, Modifier.weight(1f).aspectRatio(1f))
            MetricCard("🗑️", s.garbageDrop, s.garbageDropStatus, s.today, orange50h, Color(0xFFE65100), Modifier.weight(1f).aspectRatio(1f))
            MetricCard("💡", s.electricityUsage, s.electricityValue, s.today, blue50h, blue600h, Modifier.weight(1f).aspectRatio(1f))
        }
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("💰", s.greenSpending, s.greenSpendingValue, s.greenSpendingMonth, Color(0xFFFCE4EC), Color(0xFFC62828), Modifier.weight(1f).aspectRatio(1f))
            MetricCard("🥦", s.greenMealMetric, s.greenMealPercent, s.greenMealSubtitle, green50h, green600h, Modifier.weight(1f).aspectRatio(1f))
            MetricCard("✅", s.todosMetric, s.todosValue, s.today, Color(0xFFF3E5F5), Color(0xFF6A1B9A), Modifier.weight(1f).aspectRatio(1f))
        }
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            MetricCard("🚶", s.walkDistance, s.walkValue, s.today, Color(0xFFE8EAF6), Color(0xFF3949AB), Modifier.weight(1f).aspectRatio(1f))
            Spacer(Modifier.weight(2f))
        }

        GarbageHeatmapCard()
        HouseholdElectricityCard()
        GreenMealCard()
        TodosSummaryCard()

        Text(s.features, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton("📷", s.wasteSort, s.wasteSortDesc, green50h, green800h, Modifier.weight(1f)) { }
                FeatureButton("🗑️", s.garbageDrop, s.garbageDropDesc, orange50h, Color(0xFFE65100), Modifier.weight(1f)) { }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton("📋", s.todos, s.todosDesc, Color(0xFFF3E5F5), Color(0xFF6A1B9A), Modifier.weight(1f)) { }
                FeatureButton("💡", s.electricityUsage, s.electricityChartDesc, blue50h, blue600h, Modifier.weight(1f)) { }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton("🍽️", s.scanMeal, s.scanMealDesc, green50h, green600h, Modifier.weight(1f)) { }
                Spacer(Modifier.weight(1f))
            }
        }
    }
}
