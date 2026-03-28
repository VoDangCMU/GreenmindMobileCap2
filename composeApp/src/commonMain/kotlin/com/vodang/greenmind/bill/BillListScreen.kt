package com.vodang.greenmind.bill

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.BillRecord
import com.vodang.greenmind.store.BillStore

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)
private val orange   = Color(0xFFE65100)
private val red      = Color(0xFFC62828)

private fun ratioColor(ratio: Int): Color = when {
    ratio >= 70 -> green800
    ratio >= 40 -> orange
    else        -> red
}

private fun formatAmount(amount: Double): String = "$${"%.2f".format(amount)}"

@Composable
fun BillListScreen(onScanClick: () -> Unit) {
    val s = LocalAppStrings.current
    val bills by BillStore.bills.collectAsState()

    if (bills.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🧾", fontSize = 48.sp)
                    Text(s.billListEmpty, color = Color.Gray, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(green800)
                            .clickable { onScanClick() }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("📷", fontSize = 15.sp)
                        Text(s.billScanTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp)
        ) {
            items(bills, key = { it.id }) { bill ->
                BillRecordCard(bill)
            }
        }
    }
}

@Composable
private fun BillRecordCard(bill: BillRecord) {
    val s = LocalAppStrings.current
    val color = ratioColor(bill.greenRatio)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text("${bill.greenRatio}%", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(bill.storeName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1B5E20))
                Text(s.billGreenRatio(bill.greenRatio), fontSize = 12.sp, color = color)
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(formatAmount(bill.totalAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
                Text(s.billGreenAmount(bill.greenAmount), fontSize = 11.sp, color = green800)
            }
        }
    }
}
