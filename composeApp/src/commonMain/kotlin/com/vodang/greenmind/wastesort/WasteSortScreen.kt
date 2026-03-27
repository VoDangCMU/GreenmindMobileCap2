package com.vodang.greenmind.wastesort

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)

@Composable
fun WasteSortScreen(onScanClick: () -> Unit = {}) {
    val s = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(green50),
                contentAlignment = Alignment.Center
            ) {
                Text("♻️", fontSize = 40.sp)
            }
            Text(
                text = s.wasteSort,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = green800
            )
            Text(
                text = s.wasteSortDesc,
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }

        // Scan button
        Button(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = green600)
        ) {
            Text("📷  ${s.wasteSort}", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        // Waste type guide
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(green50)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Waste types",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
            WasteTypeRow("🟢", "Organic", "Food scraps, leaves", Color(0xFF388E3C))
            WasteTypeRow("🔵", "Recyclable", "Paper, plastic, glass", Color(0xFF1976D2))
            WasteTypeRow("🔴", "Hazardous", "Batteries, chemicals", Color(0xFFD32F2F))
            WasteTypeRow("⚫", "General", "Non-recyclable waste", Color(0xFF424242))
        }
    }
}

@Composable
private fun WasteTypeRow(dot: String, name: String, desc: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(dot, fontSize = 16.sp)
        }
        Column {
            Text(name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1B1B1B))
            Text(desc, fontSize = 12.sp, color = Color.Gray)
        }
    }
}
