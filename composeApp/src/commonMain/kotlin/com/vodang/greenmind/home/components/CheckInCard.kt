package com.vodang.greenmind.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800c = Color(0xFF2E7D32)
private val green50c  = Color(0xFFE8F5E9)

@Composable
fun CheckInCard(points: List<WastePoint>, onCheckInClick: (reportId: String) -> Unit) {
    val s = LocalAppStrings.current
    val nextPoint = points.firstOrNull { !it.collected }
    SectionCard {
        Text(s.checkInCardTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        if (nextPoint == null) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("🎉", fontSize = 40.sp)
                Text(s.checkInDone, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = green800c, textAlign = TextAlign.Center)
                Text(s.checkInDoneDesc, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
            }
        } else {
            Surface(shape = RoundedCornerShape(10.dp), color = green50c, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(s.nextPointLabel, fontSize = 11.sp, color = Color.Gray)
                    Text(nextPoint.address, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("${nextPoint.zone} · ${s.bagsEstimatedFmt(nextPoint.bags)}", fontSize = 12.sp, color = Color.Gray)
                }
            }
            Spacer(Modifier.height(10.dp))
            Surface(shape = RoundedCornerShape(10.dp), color = green800c, modifier = Modifier.fillMaxWidth(), onClick = { onCheckInClick(nextPoint.reportId) }) {
                Text(s.checkInButton, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 14.dp))
            }
        }
    }
}
