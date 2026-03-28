package com.vodang.greenmind.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.BillStore
import com.vodang.greenmind.store.MealStore
import com.vodang.greenmind.time.currentTimeMillis

private val green800h = Color(0xFF2E7D32)
private val green600h = Color(0xFF388E3C)
private val green50h  = Color(0xFFE8F5E9)
private val blue600h  = Color(0xFF1976D2)
private val blue50h   = Color(0xFFE3F2FD)
private val orange50h = Color(0xFFFFF3E0)
private val red600    = Color(0xFFC62828)

private fun isToday(timestampMillis: Long): Boolean =
    (currentTimeMillis() - timestampMillis) < 86_400_000L

@Composable
fun HouseholdDashboard(
    scrollState: ScrollState = rememberScrollState(),
    onScanMealClick: () -> Unit = {},
    onScanBillClick: () -> Unit = {},
    onWasteSortClick: () -> Unit = {},
    onTodosClick: () -> Unit = {},
    onElectricityClick: () -> Unit = {},
    onGarbageDropClick: () -> Unit = {},
) {
    val s = LocalAppStrings.current
    val meals by MealStore.meals.collectAsState()
    val bills by BillStore.bills.collectAsState()

    LaunchedEffect(Unit) { /* stores are loaded on demand */ }

    val todayMeals = meals.filter { isToday(it.timestampMillis) }
    val todayBills = bills.filter { isToday(it.timestampMillis) }

    // Waste count: number of eco-disposal actions today
    val wasteCount = todayMeals.size + todayBills.size

    // Greener score: average green ratio from today's meals + bills
    val allRatios = todayMeals.map { it.plantRatio } + todayBills.map { it.greenRatio }
    val greenerScore = if (allRatios.isEmpty()) 0 else allRatios.average().toInt()
    val isGreener = greenerScore >= 50

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OceanScoreCard()

        // TODO: Replace with real scan result once YoloScanResult DTO is wired in.
        EnvImpactCard()

        Text(s.todayOverview, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Waste today card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = orange50h),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.weight(1f).aspectRatio(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🗑️", fontSize = 28.sp)
                    Column {
                        Text(
                            text = s.wasteBagsCount(wasteCount),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE65100)
                        )
                        Text(s.wasteToday, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    }
                }
            }

            // Greener indicator card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = green50h),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.weight(1f).aspectRatio(1f)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("🌿", fontSize = 28.sp)
                    Column {
                        if (allRatios.isEmpty()) {
                            Text(
                                text = s.noActivityToday,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                        } else {
                            Text(
                                text = "$greenerScore%",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isGreener) green800h else red600
                            )
                            Text(
                                text = if (isGreener) s.greenerUp else s.greenerDown,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isGreener) green600h else red600
                            )
                        }
                        Text(s.greenerLabel, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.DarkGray)
                    }
                }
            }
        }

        Text(s.features, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // TODO: Wire up navigation to WasteSortScreen (same as onWasteSortClick param).
                FeatureButton("📷", s.wasteSort, s.wasteSortDesc, green50h, green800h, Modifier.weight(1f)) { }
                FeatureButton("🗑️", s.garbageDrop, s.garbageDropDesc, orange50h, Color(0xFFE65100), Modifier.weight(1f)) { onGarbageDropClick() }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton("📋", s.todos, s.todosDesc, Color(0xFFF3E5F5), Color(0xFF6A1B9A), Modifier.weight(1f)) { onTodosClick() }
                FeatureButton("💡", s.electricityUsage, s.electricityChartDesc, blue50h, blue600h, Modifier.weight(1f)) { onElectricityClick() }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton("🍽️", s.scanMeal, s.scanMealDesc, green50h, green600h, Modifier.weight(1f)) { onScanMealClick() }
                FeatureButton("🧾", s.scanBill, s.scanBillDesc, Color(0xFFFFF8E1), Color(0xFFF57F17), Modifier.weight(1f)) { onScanBillClick() }
            }
        }
    }
}
