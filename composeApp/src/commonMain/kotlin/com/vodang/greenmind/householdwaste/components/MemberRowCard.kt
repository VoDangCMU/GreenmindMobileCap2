package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.HouseholdMemberDto
import com.vodang.greenmind.i18n.LocalAppStrings

private val green700 = Color(0xFF2E7D32)
private val green50s = Color(0xFFE8F5E9)
private val gray700s = Color(0xFF374151)
private val gray400s = Color(0xFF9CA3AF)
private val red600s  = Color(0xFFDC2626)

@Composable
internal fun MemberRow(
    member: HouseholdMemberDto,
    isRemoving: Boolean,
    onRemove: () -> Unit,
) {
    val s = LocalAppStrings.current

    val initials = member.fullName
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifBlank { member.username.take(2).uppercase() }

    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar circle
            Box(
                Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(green50s),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = green700)
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    member.fullName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = gray700s,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "@${member.username}",
                    fontSize = 12.sp,
                    color = gray400s
                )
                Text(
                    member.email,
                    fontSize = 11.sp,
                    color = gray400s,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Role badge
            Box(
                Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(green50s)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    member.role.replaceFirstChar { it.uppercaseChar() },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = green700
                )
            }

            // Remove button
            if (isRemoving) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = red600s
                )
            } else {
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Text(s.removeMember, fontSize = 14.sp, color = red600s)
                }
            }
        }
    }
}