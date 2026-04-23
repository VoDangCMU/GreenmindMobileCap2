package com.vodang.greenmind.householdwaste

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.vodang.greenmind.api.households.addMemberToHousehold
import com.vodang.greenmind.api.households.removeMemberFromHousehold
import com.vodang.greenmind.householdwaste.components.MemberRow
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.theme.Green700
import com.vodang.greenmind.theme.Gray700Dark
import com.vodang.greenmind.theme.Gray400Neutral
import com.vodang.greenmind.theme.Red600Alert
import com.vodang.greenmind.theme.Red50Light
import kotlinx.coroutines.launch

private val green700  = Green700
private val gray700  = Gray700Dark
private val gray400  = Gray400Neutral
private val red600   = Red600Alert
private val red50    = Red50Light

@Composable
fun HouseholdSettingsScreen(onBack: () -> Unit) {
    val s = LocalAppStrings.current
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
                        Text(s.backArrow, fontSize = 22.sp, color = Color.White)
                    }
                    Column {
                        Text(
                            s.householdSettings,
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
                            s.addMember,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = gray700
                        )
                        OutlinedTextField(
                            value = addUserId,
                            onValueChange = {
                                addUserId = it
                                addError = null
                                addSuccess = false
                            },
                            label = { Text(s.userId) },
                            placeholder = { Text(s.userIdPlaceholder) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = addError != null,
                            supportingText = when {
                                addError != null -> { { Text(addError!!, color = red600) } }
                                addSuccess -> { { Text(s.memberAddedSuccess, color = green700) } }
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
                                if (uid.isBlank()) { addError = s.userIdCannotBeEmpty; return@Button }
                                scope.launch {
                                    isAdding = true
                                    addError = null
                                    try {
                                        addMemberToHousehold(token, h, uid)
                                        addUserId = ""
                                        addSuccess = true
                                        HouseholdStore.fetchHousehold()
                                    } catch (e: Throwable) {
                                        addError = s.failedToAddMember
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
                            Text(s.addMember)
                        }
                    }
                }

                // ── Members list ──────────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        s.members,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = gray700
                    )
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE5E7EB))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("${members.size}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = gray700)
                    }
                }

                if (removeError != null) {
                    Card(
                        Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = red50),
                    ) {
                        Text(
                            removeError!!,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 12.sp,
                            color = red600
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
                        Text(s.noMembersYet, fontSize = 13.sp, color = gray400)
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
                                        removeError = s.failedToRemoveMember
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