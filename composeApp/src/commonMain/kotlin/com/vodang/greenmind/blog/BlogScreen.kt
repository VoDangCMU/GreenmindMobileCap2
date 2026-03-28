package com.vodang.greenmind.blog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

@Composable
fun BlogScreen() {
    val s = LocalAppStrings.current

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Column {
                    Text(
                        s.blog,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = green800,
                    )
                    Text(
                        s.blogSubtitle,
                        fontSize = 13.sp,
                        color = Color(0xFF757575),
                    )
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text("📰", fontSize = 48.sp)
                    Text(
                        s.blogEmpty,
                        fontSize = 14.sp,
                        color = Color.Gray,
                    )
                }
            }
        }
    }
}
