package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800r = Color(0xFF2E7D32)

@Composable
fun CollectionRouteCard(points: List<WastePoint>) {
    val s = LocalAppStrings.current
    SectionCard {
        Text(s.routeCardTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(10.dp))
        points.forEachIndexed { index, point ->
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(modifier = Modifier.size(14.dp).background(if (point.collected) green800r else Color(0xFFBDBDBD), CircleShape))
                    if (index < points.lastIndex) {
                        Box(modifier = Modifier.width(2.dp).height(32.dp).background(Color(0xFFE0E0E0)))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.padding(bottom = if (index < points.lastIndex) 4.dp else 0.dp)) {
                    Text(point.address, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = if (point.collected) Color.Gray else Color.Black)
                    Text(
                        "${point.zone} · ${point.bags} ${s.bagsUnit}" + if (point.collected) " ✓" else "",
                        fontSize = 11.sp,
                        color = if (point.collected) green800r else Color.Gray
                    )
                }
            }
        }
    }
}
