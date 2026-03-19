package com.vodang.greenmind.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.UserDto
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800 = Color(0xFF2E7D32)
private val green50 = Color(0xFFE8F5E9)

@Composable
fun HomeTopBar(
    onMenuClick: () -> Unit,
    onCameraClick: () -> Unit,
    user: UserDto?,
    userType: UserType,
    onSwitchClick: () -> Unit
) {
    val s = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).clickable { onMenuClick() }.background(green50),
            contentAlignment = Alignment.Center
        ) {
            Text("☰", fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val displayName = user?.fullName ?: user?.username ?: "User"
            Text(s.greeting(displayName), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = green800)
            Text(s.welcomeBack, fontSize = 12.sp, color = Color.Gray)
        }
        val roleLabel = when (userType) {
            UserType.HOUSEHOLD -> s.householdRole
            UserType.COLLECTOR -> s.collectorRole
            UserType.VOLUNTEER -> s.volunteerRole
        }
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(green50)
                .clickable { onSwitchClick() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(userType.icon, fontSize = 16.sp)
            Text("⇄", fontSize = 16.sp, color = green800, fontWeight = FontWeight.Bold)
        }
    }
}
