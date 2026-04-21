package com.vodang.greenmind.home.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Celebration
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.store.haversineMeters

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
                Icon(Icons.Filled.Celebration, contentDescription = null, modifier = Modifier.size(48.dp), tint = green800c)
                Text(s.checkInDone, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = green800c, textAlign = TextAlign.Center)
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
            
            val currentLoc by Geo.service.locationUpdates.collectAsState(initial = null)
            val nextLat = nextPoint.lat
            val nextLng = nextPoint.lng
            
            var isTooFar = false
            var distanceMeters: Double? = null
            
            if (currentLoc != null && nextLat != null && nextLng != null) {
                distanceMeters = haversineMeters(currentLoc!!.latitude, currentLoc!!.longitude, nextLat, nextLng)
                isTooFar = distanceMeters > 50.0
            }
            
            val btnColor = if (nextLat != null && currentLoc == null) Color.Gray
                           else if (isTooFar) Color.Gray
                           else green800c
                           
            val btnText = if (nextLat != null && currentLoc == null) "Đang lấy vị trí..."
                          else if (isTooFar) "Khoảng cách quá xa (${distanceMeters?.toInt()}m)"
                          else s.checkInButton

            Surface(
                shape = RoundedCornerShape(10.dp), 
                color = btnColor, 
                modifier = Modifier.fillMaxWidth(), 
                onClick = { 
                    if (nextLat == null || (currentLoc != null && !isTooFar)) {
                        onCheckInClick(nextPoint.reportId) 
                    }
                }
            ) {
                Text(btnText, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 14.dp))
            }
        }
    }
}
