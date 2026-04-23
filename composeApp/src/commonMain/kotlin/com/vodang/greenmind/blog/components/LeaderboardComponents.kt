package com.vodang.greenmind.blog.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.blog.LeaderboardEntryDto
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.Green100
import com.vodang.greenmind.theme.Gray400
import com.vodang.greenmind.theme.Gray500
import com.vodang.greenmind.theme.Gray600

private val green800 = Green800
private val green100 = Green100
private val gray400 = Gray400
private val gray500 = Gray500
private val gray600 = Gray600

private fun authorInitials(name: String, username: String): String =
    name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("")
        .ifBlank { username.take(2).uppercase() }

@Composable
fun LeaderboardTab(
    accessToken: String,
    onLoad: suspend () -> List<LeaderboardEntryDto>,
) {
    val s = LocalAppStrings.current
    var entries by remember { mutableStateOf<List<LeaderboardEntryDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(accessToken) {
        isLoading = true
        error = null
        try {
            entries = onLoad()
        } catch (e: Throwable) {
            error = e.message ?: s.blogErrorLoad
        }
        isLoading = false
    }

    Column(Modifier.fillMaxSize()) {
        Surface(modifier = Modifier.fillMaxWidth(), color = Color.White) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFFFFD700),
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(s.leaderboardTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Gray600)
                    Text(s.leaderboardSubtitle, fontSize = 12.sp, color = gray400)
                }
            }
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green800)
            }
            error != null -> Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(error!!, fontSize = 14.sp, color = gray500, textAlign = TextAlign.Center)
                    Button(onClick = { }, colors = ButtonDefaults.buttonColors(containerColor = green800), shape = RoundedCornerShape(10.dp)) {
                        Text(s.blogRetry)
                    }
                }
            }
            entries.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("🏆", fontSize = 40.sp)
                    Text(s.leaderboardEmpty, fontSize = 14.sp, color = gray500)
                }
            }
            else -> LazyColumn(
                Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val top3 = entries.take(3)
                if (top3.size == 3) {
                    item { PodiumRow(top3) }
                    item { Spacer(Modifier.height(4.dp)) }
                }
                val rest = if (entries.size > 3) entries.drop(3) else entries
                items(rest, key = { it.userId }) { entry -> LeaderboardRow(entry = entry) }
            }
        }
    }
}

@Composable
fun PodiumRow(top3: List<LeaderboardEntryDto>) {
    if (top3.size != 3) return
    val order = listOf(top3[1], top3[0], top3[2])
    val medals = listOf("🥈", "🥇", "🥉")
    val bgColors = listOf(Color(0xFFB0BEC5), Color(0xFFFFD700), Color(0xFFBF8970))
    val gray500Color = Gray500

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(Modifier.padding(top = 16.dp, bottom = 12.dp, start = 12.dp, end = 12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom,
            ) {
                order.forEachIndexed { i, entry ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(medals[i], fontSize = 28.sp)
                        val initials = authorInitials(entry.fullName, entry.username)
                        Box(
                            Modifier.size(44.dp).clip(CircleShape).background(bgColors[i]),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(initials, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Text(
                            entry.fullName.split(" ").firstOrNull() ?: entry.username,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF374151),
                        )
                        Box(
                            Modifier.width(80.dp).height(listOf(72.dp, 96.dp, 56.dp)[i])
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(bgColors[i].copy(alpha = 0.25f)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("#${entry.rank}", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = bgColors[i])
                                Text("${entry.reportCount} rpts", fontSize = 10.sp, color = gray500Color)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(entry: LeaderboardEntryDto) {
    val s = LocalAppStrings.current
    val initials = authorInitials(entry.fullName, entry.username)

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(Color(0xFFF3F4F6)),
                contentAlignment = Alignment.Center,
            ) {
                Text("#${entry.rank}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = gray500)
            }
            Box(
                Modifier.size(36.dp).clip(CircleShape).background(green100),
                contentAlignment = Alignment.Center,
            ) {
                Text(initials, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = green800)
            }
            Column(Modifier.weight(1f)) {
                Text(entry.fullName, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1C1B1F))
                Text("@${entry.username}", fontSize = 11.sp, color = gray400)
            }
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFA5D6A7)) {
                Text(
                    s.leaderboardReports(entry.reportCount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = green800,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
        }
    }
}