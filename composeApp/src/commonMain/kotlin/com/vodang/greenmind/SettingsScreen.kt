package com.vodang.greenmind

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.home.components.GreenAppBar
import com.vodang.greenmind.home.components.LanguagePickerModal
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.GpsTick
import com.vodang.greenmind.store.LocationTrackingStore
import com.vodang.greenmind.store.SettingsStore

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val s = LocalAppStrings.current

    val intervalMs      by SettingsStore.locationIntervalMs.collectAsState()
    val minMove         by SettingsStore.minMoveMeters.collectAsState()
    val maxSpeed        by SettingsStore.maxWalkSpeedMs.collectAsState()
    val trackingEnabled by SettingsStore.locationEnabled.collectAsState()
    val language        by SettingsStore.language.collectAsState()
    val recentTicks     by LocationTrackingStore.recentTicks.collectAsState()

    // Local slider state — committed to store only on finger-lift to avoid excessive writes
    var sliderMove by remember(minMove) { mutableStateOf(minMove) }
    var showLangPicker by remember { mutableStateOf(false) }

    val intervalOptions = listOf(
        15_000L  to "15s",
        30_000L  to "30s",
        55_000L  to "55s",
        120_000L to "2m",
        300_000L to "5m",
    )

    // Speed presets: value (m/s) → label
    val speedPresets = listOf(
        2.5f  to "🚶 ${s.settingsSpeedWalk}",
        7.0f  to "🏃 ${s.settingsSpeedRun}",
        15.0f to "🚴 ${s.settingsSpeedCycle}",
    )
    val selectedSpeedIdx = speedPresets.indices.minByOrNull {
        kotlin.math.abs(speedPresets[it].first - maxSpeed)
    } ?: 1

    val currentLangLabel = if (language == "vi") "🇻🇳 ${s.langVietnamese}" else "🇬🇧 ${s.langEnglish}"

    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            GreenAppBar(
                title = s.settings,
                onBack = onBack,
                scrolled = scrollState.value > 0,
            )
        },
        containerColor = Color(0xFFF1F8E9),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(innerPadding),
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {

                // ── Location Tracking ─────────────────────────────────────────
                SettingsSectionHeader("🗺  ${s.settingsLocation}")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {

                        // Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(s.settingsTrackingEnabled, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                                Text(s.settingsTrackingEnabledDesc, fontSize = 12.sp, color = Color.Gray)
                            }
                            Switch(
                                checked = trackingEnabled,
                                onCheckedChange = { SettingsStore.setLocationEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = green800),
                            )
                        }

                        HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))

                        // Interval
                        Text(s.settingsInterval, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                        Text(s.settingsIntervalDesc, fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            intervalOptions.forEach { (ms, label) ->
                                val selected = intervalMs == ms
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selected) green800 else Color(0xFFF5F5F5),
                                    modifier = Modifier.clickable { SettingsStore.setLocationInterval(ms) },
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 13.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.White else Color.DarkGray,
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))

                        // Stationary threshold slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(s.settingsMinMove, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121), modifier = Modifier.weight(1f))
                            Text(
                                "${sliderMove.toInt()} m",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = green800,
                            )
                        }
                        Text(s.settingsMinMoveDesc(sliderMove.toInt()), fontSize = 12.sp, color = Color.Gray)
                        Slider(
                            value = sliderMove,
                            onValueChange = { sliderMove = it },
                            onValueChangeFinished = { SettingsStore.setMinMoveMeters(sliderMove) },
                            valueRange = 5f..100f,
                            steps = 18,  // every 5m
                            colors = SliderDefaults.colors(thumbColor = green800, activeTrackColor = green800, inactiveTrackColor = green50),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))

                        // Speed filter
                        Text(s.settingsSpeedFilter, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                        Text(s.settingsSpeedFilterDesc(speedPresets[selectedSpeedIdx].first), fontSize = 12.sp, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            speedPresets.forEachIndexed { idx, (speed, label) ->
                                val selected = idx == selectedSpeedIdx
                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (selected) green800 else Color(0xFFF5F5F5),
                                    modifier = Modifier.clickable { SettingsStore.setMaxWalkSpeedMs(speed) },
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.White else Color.DarkGray,
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Recent GPS ticks ──────────────────────────────────────────
                SettingsSectionHeader("📡  Recent GPS records")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        if (recentTicks.isEmpty()) {
                            Text(
                                "No records yet — tracking will populate this list.",
                                fontSize = 13.sp,
                                color = Color.Gray,
                            )
                        } else {
                            recentTicks.asReversed().forEachIndexed { idx, tick ->
                                if (idx > 0) HorizontalDivider(color = Color(0xFFF0F0F0))
                                GpsTickRow(tick)
                            }
                        }
                    }
                }

                // ── Network Log ───────────────────────────────────────────────
                NetworkLogSection()

                // ── General ───────────────────────────────────────────────────
                SettingsSectionHeader("⚙  ${s.settingsGeneral}")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(4.dp)) {
                        SettingsNavRow(
                            icon = "🌐",
                            title = s.language,
                            subtitle = currentLangLabel,
                            onClick = { showLangPicker = true },
                        )
                    }
                }

                // ── About ─────────────────────────────────────────────────────
                SettingsSectionHeader("ℹ  ${s.settingsAbout}")
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("🌱", fontSize = 22.sp)
                            Spacer(Modifier.width(10.dp))
                            Text("GreenMind", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = green800, modifier = Modifier.weight(1f))
                            Text("v1.0.0", fontSize = 13.sp, color = Color.Gray)
                        }

                        HorizontalDivider(color = Color(0xFFF0F0F0), modifier = Modifier.padding(vertical = 8.dp))

                        Text(s.settingsHowWorks, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF212121))
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = s.settingsHowWorksBody(
                                (intervalMs / 1000).toInt(),
                                minMove.toInt(),
                                maxSpeed,
                            ),
                            fontSize = 13.sp,
                            color = Color(0xFF616161),
                            lineHeight = 20.sp,
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }

    if (showLangPicker) {
        LanguagePickerModal(
            currentLang = language,
            onSelect = { SettingsStore.setLanguage(it) },
            onDismiss = { showLangPicker = false },
        )
    }
}

// ── Private helpers ───────────────────────────────────────────────────────────

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF424242),
        modifier = Modifier.padding(start = 4.dp),
    )
}

@Composable
private fun GpsTickRow(tick: GpsTick) {
    // Format HH:mm:ss from epoch millis (UTC — avoids expect/actual for time formatting)
    val totalSec = (tick.timestampMs / 1000L).toInt()
    val hh = (totalSec / 3600) % 24
    val mm = (totalSec / 60)   % 60
    val ss = totalSec % 60
    val timeLabel = "%02d:%02d:%02d".format(hh, mm, ss)

    val distLabel = if (tick.distanceMeters < 1.0) "stationary"
                    else "%.0f m".format(tick.distanceMeters)

    val addrShort = if (tick.address.length > 40) tick.address.take(37) + "…" else tick.address

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        // Time badge
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFE8F5E9),
        ) {
            Text(
                timeLabel,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = green800,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                "%.5f, %.5f".format(tick.latitude, tick.longitude),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
            )
            if (addrShort.isNotBlank()) {
                Text(addrShort, fontSize = 11.sp, color = Color.Gray)
            }
        }
        Text(
            distLabel,
            fontSize = 11.sp,
            color = if (tick.distanceMeters < 1.0) Color(0xFF9E9E9E) else green800,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun SettingsNavRow(icon: String, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(icon, fontSize = 20.sp)
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF212121), modifier = Modifier.weight(1f))
        Text(subtitle, fontSize = 13.sp, color = Color.Gray)
        Text("›", fontSize = 18.sp, color = Color.LightGray)
    }
}
