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
import com.vodang.greenmind.api.auth.UserDto
import com.vodang.greenmind.i18n.LocalAppStrings

private val green100 = Color(0xFFC8E6C9)

@Composable
fun ProfilePlaceholder(user: UserDto? = null) {
    val s = LocalAppStrings.current
    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(32.dp))
        Box(modifier = Modifier.size(96.dp).background(green100, CircleShape), contentAlignment = Alignment.Center) {
            Text("👤", fontSize = 48.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text(user?.fullName ?: s.profileName, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(user?.email ?: s.profileEmail, fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(32.dp))
        SectionCard { Text(s.editProfile, fontWeight = FontWeight.Medium) }
    }
}
