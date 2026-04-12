package com.vodang.greenmind.householdwaste

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.households.HouseholdDto
import com.vodang.greenmind.api.households.HouseholdMemberDto
import com.vodang.greenmind.api.households.addMemberToHousehold
import com.vodang.greenmind.api.households.removeMemberFromHousehold
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.launch

private val green700  = Color(0xFF2E7D32)
private val green50s  = Color(0xFFE8F5E9)
private val gray700s  = Color(0xFF374151)
private val gray400s  = Color(0xFF9CA3AF)
private val red600s   = Color(0xFFDC2626)
private val red50s    = Color(0xFFFEF2F2)

@Composable
fun HouseholdSettingsScreen(onBack: () -> Unit) {
    val household by HouseholdStore.household.collectAsState()
    val h = household ?: run { onBack(); return }
    val token = SettingsStore.getAccessToken() ?: run { onBack(); return }

    val scope = rememberCoroutineScope()

    var addUserId     by remember { mutableStateOf("") }
    var isAdding      by remember { mutableStateOf(false) }
    var addError      by remember { mutableStateOf<String?>(null) }
    var addSuccess    by remember { mutableStateOf(false) }
    var removingId    by remember { mutableStateOf<String?>(null) }
    var removeError   by remember { mutableStateOf<String?>(null) }

    // local members list so we can reflect removes immediately
    var members by remember(h) { mutableStateOf(h.members ?: emptyList()) }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6))
    ) {
        Column(Modifier.fillMaxSize()) {

            // ── Header ────────────────────────────────────────────────────────
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(green700)
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(onClick = onBack) {
                        Text("←", fontSize = 22.sp, color = Color.White)
                    }
                    Column {
                        Text(
                            "Household Settings",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            h.address,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // ── Add member card ───────────────────────────────────────────
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Add Member",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = gray700s
                        )
                        OutlinedTextField(
                            value = addUserId,
                            onValueChange = {
                                addUserId = it
                                addError = null
                                addSuccess = false
                            },
                            label = { Text("User ID") },
                            placeholder = { Text("Enter the member's user ID") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = addError != null,
                            supportingText = when {
                                addError != null -> { { Text(addError!!, color = red600s) } }
                                addSuccess -> { { Text("Member added successfully", color = green700) } }
                                else -> null
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = green700,
                                unfocusedBorderColor = Color(0xFFDDDDDD),
                            ),
                        )
                        Button(
                            onClick = {
                                val uid = addUserId.trim()
                                if (uid.isBlank()) { addError = "User ID cannot be empty"; return@Button }
                                scope.launch {
                                    isAdding = true
                                    addError = null
                                    try {
                                        addMemberToHousehold(token, h, uid)
                                        addUserId = ""
                                        addSuccess = true
                                        HouseholdStore.fetchHousehold()
                                    } catch (e: Throwable) {
                                        addError = e.message ?: "Failed to add member"
                                    } finally {
                                        isAdding = false
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = green700),
                            enabled = !isAdding,
                        ) {
                            if (isAdding) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text("Add Member")
                        }
                    }
                }

                // ── Members list ──────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Members",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = gray700s
                    )
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE5E7EB))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("${members.size}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = gray700s)
                    }
                }

                if (removeError != null) {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = red50s),
                    ) {
                        Text(
                            removeError!!,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = red600s
                        )
                    }
                }

                if (members.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No members yet", fontSize = 13.sp, color = gray400s)
                    }
                } else {
                    members.forEach { member ->
                        MemberRow(
                            member      = member,
                            isRemoving  = removingId == member.id,
                            onRemove    = {
                                scope.launch {
                                    removingId = member.id
                                    removeError = null
                                    try {
                                        removeMemberFromHousehold(token, member.id)
                                        members = members.filter { it.id != member.id }
                                        HouseholdStore.fetchHousehold()
                                    } catch (e: Throwable) {
                                        removeError = e.message ?: "Failed to remove member"
                                    } finally {
                                        removingId = null
                                    }
                                }
                            },
                        )
                    }
                }

                Spacer(Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun MemberRow(
    member: HouseholdMemberDto,
    isRemoving: Boolean,
    onRemove: () -> Unit,
) {
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
                    Text("✕", fontSize = 14.sp, color = red600s)
                }
            }
        }
    }
}
