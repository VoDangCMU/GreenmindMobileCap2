package com.vodang.greenmind.home.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.UserDto
import com.vodang.greenmind.api.campaign.CampaignDto
import com.vodang.greenmind.api.campaign.getAllCampaigns
import com.vodang.greenmind.api.participantcampaign.ParticipantCampaignDto
import com.vodang.greenmind.api.participantcampaign.checkInCampaign
import com.vodang.greenmind.api.participantcampaign.checkOutCampaign
import com.vodang.greenmind.api.participantcampaign.registerCampaign
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.store.SettingsStore
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

private val green800v = Color(0xFF2E7D32)
private val green600v = Color(0xFF388E3C)
private val green50v  = Color(0xFFE8F5E9)
private val blue600v  = Color(0xFF1976D2)
private val blue50v   = Color(0xFFE3F2FD)
private val teal600v  = Color(0xFF00897B)
private val teal50v   = Color(0xFFE0F2F1)
private val orange600v = Color(0xFFF57C00)
private val orange50v  = Color(0xFFFFF3E0)

// Per-campaign mutable state tracked in memory for this session
private data class CampaignUiState(
    val participant: ParticipantCampaignDto? = null,
    val busy: Boolean = false,
    val error: String? = null,
)

@Composable
fun VolunteerDashboard(user: UserDto? = null, scrollState: ScrollState = rememberScrollState()) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var campaigns by remember { mutableStateOf<List<CampaignDto>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    // Track participant state per campaign id
    val campaignStates = remember { mutableStateMapOf<String, CampaignUiState>() }

    val accessToken = remember { SettingsStore.getAccessToken() ?: "" }

    // Load campaigns once
    LaunchedEffect(accessToken) {
        if (accessToken.isBlank()) { loading = false; return@LaunchedEffect }
        loading = true
        loadError = null
        try {
            campaigns = getAllCampaigns(accessToken)
        } catch (e: Throwable) {
            loadError = e.message ?: s.volunteerLoadError
        } finally {
            loading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OceanScoreCard()

        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MetricCard("⏱️", s.volunteerHoursLabel, s.volunteerHoursValue, "", teal50v, teal600v, Modifier.weight(1f).aspectRatio(1f))
            MetricCard("🗓️", s.volunteerEventsLabel, s.volunteerEventsValue, "", green50v, green800v, Modifier.weight(1f).aspectRatio(1f))
            MetricCard("⭐", s.volunteerPointsLabel, s.volunteerPointsValue, "", blue50v, blue600v, Modifier.weight(1f).aspectRatio(1f))
        }

        GarbageHeatmapCard()

        // Campaign list section
        SectionCard {
            Text(s.volunteerEventsCardTitle, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Spacer(Modifier.height(12.dp))

            when {
                loading -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = green800v)
                            Text(s.volunteerLoading, fontSize = 13.sp, color = Color.Gray)
                        }
                    }
                }
                loadError != null -> {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("⚠️ $loadError", fontSize = 13.sp, color = Color(0xFFB71C1C))
                        TextButton(onClick = {
                            scope.launch {
                                loading = true
                                loadError = null
                                try { campaigns = getAllCampaigns(accessToken) }
                                catch (e: Throwable) { loadError = e.message ?: s.volunteerLoadError }
                                finally { loading = false }
                            }
                        }) {
                            Text("Retry", fontSize = 12.sp, color = green800v)
                        }
                    }
                }
                campaigns.isEmpty() -> {
                    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        Text(s.volunteerNoCampaigns, fontSize = 13.sp, color = Color.Gray)
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        campaigns.forEach { campaign ->
                            val state = campaignStates[campaign.id] ?: CampaignUiState()
                            CampaignRow(
                                campaign = campaign,
                                state = state,
                                s = s,
                                onRegister = {
                                    scope.launch {
                                        campaignStates[campaign.id] = state.copy(busy = true, error = null)
                                        try {
                                            val result = registerCampaign(accessToken, campaign.id)
                                            campaignStates[campaign.id] = CampaignUiState(participant = result)
                                        } catch (e: Throwable) {
                                            campaignStates[campaign.id] = state.copy(busy = false, error = e.message ?: "Error")
                                        }
                                    }
                                },
                                onCheckIn = {
                                    scope.launch {
                                        val participant = state.participant ?: return@launch
                                        campaignStates[campaign.id] = state.copy(busy = true, error = null)
                                        try {
                                            val loc = Geo.service.locationUpdates.firstOrNull()
                                            val lat = loc?.latitude ?: 0.0
                                            val lng = loc?.longitude ?: 0.0
                                            val result = checkInCampaign(accessToken, participant.id, lat, lng)
                                            campaignStates[campaign.id] = CampaignUiState(participant = result)
                                        } catch (e: Throwable) {
                                            campaignStates[campaign.id] = state.copy(busy = false, error = e.message ?: "Error")
                                        }
                                    }
                                },
                                onCheckOut = {
                                    scope.launch {
                                        val participant = state.participant ?: return@launch
                                        campaignStates[campaign.id] = state.copy(busy = true, error = null)
                                        try {
                                            val loc = Geo.service.locationUpdates.firstOrNull()
                                            val lat = loc?.latitude ?: 0.0
                                            val lng = loc?.longitude ?: 0.0
                                            val result = checkOutCampaign(accessToken, participant.id, lat, lng)
                                            campaignStates[campaign.id] = CampaignUiState(participant = result)
                                        } catch (e: Throwable) {
                                            campaignStates[campaign.id] = state.copy(busy = false, error = e.message ?: "Error")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CampaignRow(
    campaign: CampaignDto,
    state: CampaignUiState,
    s: com.vodang.greenmind.i18n.AppStrings,
    onRegister: () -> Unit,
    onCheckIn: () -> Unit,
    onCheckOut: () -> Unit,
) {
    val participant = state.participant
    val participantStatus = participant?.status

    // Derive isActive from campaign dates if status not present
    val isActive = campaign.status?.equals("ACTIVE", ignoreCase = true) == true ||
            campaign.status?.equals("IN_PROGRESS", ignoreCase = true) == true

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF9FBF9))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isActive) green50v else Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Text(if (isActive) "🟢" else "📅", fontSize = 18.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(campaign.name, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                Spacer(Modifier.height(2.dp))
                Text("📝 ${campaign.description}", fontSize = 11.sp, color = Color.Gray, maxLines = 2)
                Text("🕐 ${formatDate(campaign.startDate)} → ${formatDate(campaign.endDate)}", fontSize = 11.sp, color = Color.Gray)
                Text("📍 ${campaign.lat}, ${campaign.lng}  ·  radius ${campaign.radius}m", fontSize = 11.sp, color = Color.Gray)
            }
        }

        // Error row
        if (state.error != null) {
            Text("⚠️ ${state.error}", fontSize = 11.sp, color = Color(0xFFB71C1C))
        }

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.busy) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = green800v)
            } else {
                when (participantStatus) {
                    null -> {
                        // Not registered
                        ActionButton(s.volunteerJoinButton, green800v, onRegister)
                    }
                    "REGISTERED" -> {
                        // Registered, not checked in
                        StatusBadge("✓ ${s.volunteerRegistered}", green600v, green50v)
                        Spacer(Modifier.width(8.dp))
                        ActionButton(s.volunteerCheckIn, teal600v, onCheckIn)
                    }
                    "CHECKED_IN" -> {
                        // Checked in, can check out
                        StatusBadge("📍 ${s.volunteerCheckedIn}", teal600v, teal50v)
                        Spacer(Modifier.width(8.dp))
                        ActionButton(s.volunteerCheckOut, orange600v, onCheckOut)
                    }
                    "CHECKED_OUT", "COMPLETED" -> {
                        StatusBadge("✅ ${s.volunteerCheckedOut}", blue600v, blue50v)
                    }
                    else -> {
                        StatusBadge(participantStatus, Color.Gray, Color(0xFFF5F5F5))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActionButton(label: String, color: Color, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Text(label, fontSize = 12.sp, color = Color.White)
    }
}

@Composable
private fun StatusBadge(label: String, textColor: Color, bgColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(label, fontSize = 11.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}

/** Trims ISO timestamp to just the date portion for display */
private fun formatDate(iso: String): String =
    if (iso.length >= 10) iso.substring(0, 10) else iso
