package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.OceanStore

private val green800 = Color(0xFF2E7D32)

private val oceanLetters = listOf(
    "O" to Color(0xFF7B1FA2),
    "C" to Color(0xFF1565C0),
    "E" to Color(0xFF2E7D32),
    "A" to Color(0xFFE65100),
    "N" to Color(0xFFC62828)
)

@Composable
fun OceanScoreCard() {
    val s = LocalAppStrings.current
    val scores by OceanStore.scores.collectAsState()
    val isLoading by OceanStore.isLoading.collectAsState()

    LaunchedEffect(Unit) { OceanStore.load() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(s.oceanTitle, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.DarkGray)
                    Text(s.oceanSubtitle, fontSize = 11.sp, color = Color.Gray)
                }
                Box(modifier = Modifier.size(52.dp), contentAlignment = Alignment.Center) { }
            }
            Spacer(Modifier.height(12.dp))

            if (isLoading && scores == null) {
                Box(modifier = Modifier.fillMaxWidth().height(72.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = green800, modifier = Modifier.size(28.dp))
                }
            } else {
                val scoreValues = scores?.let {
                    listOf(it.O, it.C, it.E, it.A, it.N)
                } ?: listOf(0, 0, 0, 0, 0)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    oceanLetters.zip(scoreValues).forEach { (pair, score) ->
                        val (letter, color) = pair
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(36.dp).background(color.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(letter, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { score / 100f },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(6.dp)),
                                color = color,
                                trackColor = color.copy(alpha = 0.12f)
                            )
                            Spacer(Modifier.height(6.dp))
                            Text("$score", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
                        }
                    }
                }
            }
        }
    }
}
