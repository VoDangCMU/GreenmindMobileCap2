package com.vodang.greenmind.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800h = Color(0xFF2E7D32)
private val green600h = Color(0xFF388E3C)
private val green50h  = Color(0xFFE8F5E9)
private val blue600h  = Color(0xFF1976D2)
private val blue50h   = Color(0xFFE3F2FD)
private val orange50h = Color(0xFFFFF3E0)

@Composable
private fun PreAppSurveyBadge(onClick: () -> Unit) {
    val s = LocalAppStrings.current
    Card(
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF43A047)))
                )
                .padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("📋", fontSize = 32.sp)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = s.preAppSurveyBadgeTitle,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = s.preAppSurveyBadgeDesc,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f),
                    )
                }
                Text("›", fontSize = 24.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun HouseholdDashboard(
    scrollState: ScrollState = rememberScrollState(),
    onWasteSortClick: () -> Unit = {},
    onWasteImpactClick: () -> Unit = {},
    onHouseholdWasteClick: () -> Unit = {},
    onWasteStatClick: () -> Unit = {},
    onScanMealClick: () -> Unit = {},
    onScanBillClick: () -> Unit = {},
    onBlogClick: () -> Unit = {},
    showPreAppSurveyBadge: Boolean = false,
    onPreAppSurveyClick: () -> Unit = {},
) {
    val s = LocalAppStrings.current
    var showScanModal by remember { mutableStateOf(false) }

    if (showScanModal) {
        Dialog(onDismissRequest = { showScanModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(s.scanBillMealTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = green800h)
                    Spacer(Modifier.height(8.dp))
                    Text(s.scanBillMealDesc, fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FeatureButton("🍽️", s.scanMeal, s.scanMealDesc, green50h, green600h, Modifier.weight(1f)) { 
                            showScanModal = false
                            onScanMealClick() 
                        }
                        FeatureButton("🧾", s.scanBill, s.scanBillDesc, Color(0xFFFFF8E1), Color(0xFFF57F17), Modifier.weight(1f)) { 
                            showScanModal = false
                            onScanBillClick() 
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (showPreAppSurveyBadge) {
            PreAppSurveyBadge(onClick = onPreAppSurveyClick)
        }
        OceanScoreCard()

        PollutionTrendCard()

        Text(s.features, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton("📷", s.wasteSort, s.wasteSortDesc, green50h, green800h, Modifier.weight(1f)) { onWasteSortClick() }
                FeatureButton("📊", s.wasteImpactTitle, s.wasteImpactDesc, orange50h, Color(0xFFE65100), Modifier.weight(1f)) { onWasteImpactClick() }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton("📋", s.householdWasteStatusTitle, s.householdWasteStatusDesc, Color(0xFFF3E5F5), Color(0xFF6A1B9A), Modifier.weight(1f)) { onHouseholdWasteClick() }
                FeatureButton("📈", s.wasteStatTitle, s.wasteStatDesc, blue50h, blue600h, Modifier.weight(1f)) { onWasteStatClick() }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FeatureButton("📸", s.scanBillMealTitle, s.scanBillMealDesc, green50h, green600h, Modifier.weight(1f)) { showScanModal = true }
                FeatureButton("📰", s.blog, s.blogDesc, Color(0xFFFFF8E1), Color(0xFFF57F17), Modifier.weight(1f)) { onBlogClick() }
            }
        }
    }
}
