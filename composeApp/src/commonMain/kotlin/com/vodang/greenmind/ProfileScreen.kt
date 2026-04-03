package com.vodang.greenmind

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.preappsurvey.PreAppSurveyDto
import com.vodang.greenmind.api.preappsurvey.getPreAppSurveyByUser
import com.vodang.greenmind.home.components.OceanScoreCard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.BillStore
import com.vodang.greenmind.store.MealStore
import com.vodang.greenmind.store.OceanStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WalkDistanceStore
import com.vodang.greenmind.time.currentTimeMillis

private fun isToday(millis: Long) = (currentTimeMillis() - millis) < 86_400_000L

private fun formatDistance(meters: Int): String = when {
    meters < 1000 -> "${meters} m"
    else          -> "${(meters / 1000.0).fmt(2)} km"
}

@Composable
fun ProfileScreen() {
    val s = LocalAppStrings.current
    val user         by SettingsStore.user.collectAsState()
    val distMeters   by WalkDistanceStore.distanceMeters.collectAsState()
    val meals        by MealStore.meals.collectAsState()
    val bills        by BillStore.bills.collectAsState()

    LaunchedEffect(Unit) {
        WalkDistanceStore.startPolling()
        OceanStore.load()
    }

    val walkTimeMin  = (distMeters / 84).coerceAtLeast(0)
    val calories     = (distMeters / 1000.0 * 60).toInt()

    val todayMeals   = meals.filter { isToday(it.timestampMillis) }
    val todayBills   = bills.filter { isToday(it.timestampMillis) }
    val avgPlant     = if (todayMeals.isEmpty()) 0 else todayMeals.map { it.plantRatio }.average().toInt()
    val avgGreen     = if (todayBills.isEmpty()) 0 else todayBills.map { it.greenRatio }.average().toInt()

    var survey by remember { mutableStateOf<PreAppSurveyDto?>(null) }

    LaunchedEffect(user?.id) {
        val token = SettingsStore.getAccessToken() ?: return@LaunchedEffect
        val userId = user?.id ?: return@LaunchedEffect
        try {
            survey = getPreAppSurveyByUser(token, userId)
        } catch (_: ApiException) {
            // not completed yet — no section shown
        } catch (_: Throwable) { }
    }

    val green800 = Color(0xFF2E7D32)
    val green700 = Color(0xFF388E3C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F8E9))
    ) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // ── Hero card ────────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(green800, green700)))
                    .padding(horizontal = 24.dp, vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape)
                            .border(3.dp, Color.White.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("👤", fontSize = 52.sp)
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = user?.fullName?.ifBlank { null } ?: s.profileName,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = user?.email ?: s.profileEmail,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f),
                    )
                    Spacer(Modifier.height(12.dp))
                    // Role badge
                    val roleIcon = when (user?.role?.lowercase()) {
                        "collector" -> "🚛"
                        "volunteer" -> "🤝"
                        else        -> "🏠"
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.2f),
                    ) {
                        Text(
                            text = "$roleIcon  ${user?.role?.replaceFirstChar { it.uppercase() } ?: s.notSet}",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            fontSize = 13.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                    if (!user?.location.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "📍  ${user?.location}",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.75f),
                        )
                    }
                }
            }

            // ── Body ─────────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {

                // Today's Journey
                Text(s.todayJourney, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    JourneyMetricCard(
                        icon = "🚶",
                        label = s.walkDistance,
                        value = formatDistance(distMeters),
                        bg = Color(0xFFE8EAF6),
                        accent = Color(0xFF3949AB),
                        modifier = Modifier.weight(1f),
                    )
                    JourneyMetricCard(
                        icon = "⏱",
                        label = s.walkTimeLabel,
                        value = s.walkTimeMin(walkTimeMin),
                        bg = Color(0xFFE8F5E9),
                        accent = green800,
                        modifier = Modifier.weight(1f),
                    )
                    JourneyMetricCard(
                        icon = "🔥",
                        label = s.caloriesLabel,
                        value = s.caloriesKcal(calories),
                        bg = Color(0xFFFBE9E7),
                        accent = Color(0xFFE64A19),
                        modifier = Modifier.weight(1f),
                    )
                }

                // Personal Info
                Text(s.personalInfo, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        ProfileInfoRow("👤", s.fullName,       user?.fullName?.ifBlank { s.notSet } ?: s.notSet)
                        HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileInfoRow("📧", s.emailAddress,   user?.email ?: s.notSet)
                        HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileInfoRow("⚧",  s.genderLabel,    user?.gender?.replaceFirstChar { it.uppercase() } ?: s.notSet)
                        HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileInfoRow("🎂", s.ageLabel,        user?.age?.let { "$it" } ?: s.notSet)
                        HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileInfoRow("📍", s.locationLabel,  user?.location?.ifBlank { s.notSet } ?: s.notSet)
                        HorizontalDivider(color = Color(0xFFF5F5F5), modifier = Modifier.padding(horizontal = 16.dp))
                        ProfileInfoRow("🏷",  s.roleLabel,      user?.role?.replaceFirstChar { it.uppercase() } ?: s.notSet)
                    }
                }

                // OCEAN Score
                OceanScoreCard()

                // Eco Activity Today
                Text(s.ecoToday, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    EcoStatCard(
                        icon = "🍽️",
                        countLabel = s.mealsToday(todayMeals.size),
                        ratioValue = avgPlant,
                        ratioText = "$avgPlant%",
                        ratioSubtitle = "plant ratio",
                        bg = Color(0xFFE8F5E9),
                        accent = green800,
                        modifier = Modifier.weight(1f),
                    )
                    EcoStatCard(
                        icon = "🧾",
                        countLabel = s.billsToday(todayBills.size),
                        ratioValue = avgGreen,
                        ratioText = "$avgGreen%",
                        ratioSubtitle = "green spend",
                        bg = Color(0xFFFFF8E1),
                        accent = Color(0xFFF57F17),
                        modifier = Modifier.weight(1f),
                    )
                }

                // Habit Profile (pre-app survey)
                val sv = survey
                if (sv != null) {
                    val isVi = s.langCode == "vi"
                    HabitProfileSection(sv = sv, isVi = isVi, green800 = green800)
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

// ── Private helper composables ────────────────────────────────────────────────

@Composable
private fun JourneyMetricCard(
    icon: String,
    label: String,
    value: String,
    bg: Color,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(icon, fontSize = 22.sp)
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = accent)
            Text(label, fontSize = 10.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ProfileInfoRow(icon: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(icon, fontSize = 16.sp, modifier = Modifier.width(28.dp))
        Text(label, fontSize = 13.sp, color = Color.Gray, modifier = Modifier.width(100.dp))
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF212121),
            modifier = Modifier.weight(1f),
        )
    }
}

// ── Habit profile section ─────────────────────────────────────────────────────

private data class HabitRow(val emoji: String, val labelEn: String, val labelVi: String, val display: String)

private fun scaleDisplay(value: Int, max: Int = 5): String = "●".repeat(value) + "○".repeat(max - value)

@Composable
private fun HabitProfileSection(sv: com.vodang.greenmind.api.preappsurvey.PreAppSurveyDto, isVi: Boolean, green800: Color) {
    val rows = buildList {
        add(HabitRow("💸", "Daily Spending",     "Chi tiêu hàng ngày",
            "${sv.dailySpending} ${if (isVi) "đ/ngày" else "VND/day"}"))
        add(HabitRow("🚶", "Daily Distance",     "Quãng đường hàng ngày",
            "${sv.dailyDistance} km"))
        add(HabitRow("📊", "Spending Variation", "Biến động chi tiêu",
            scaleDisplay(sv.spendingVariation)))
        add(HabitRow("🛍️", "Brand Trial",        "Thử thương hiệu mới",
            scaleDisplay(sv.brandTrial)))
        add(HabitRow("📋", "Shopping List",      "Mua sắm theo danh sách",
            scaleDisplay(sv.shoppingList)))
        add(HabitRow("🗺️", "Explores New Places","Khám phá nơi mới",
            scaleDisplay(sv.newPlaces)))
        add(HabitRow("🚌", "Public Transport",   "Giao thông công cộng",
            scaleDisplay(sv.publicTransport)))
        add(HabitRow("🗓️", "Stable Schedule",    "Lịch trình ổn định",
            scaleDisplay(sv.stableSchedule)))
        add(HabitRow("🌙", "Night Outings",      "Ra ngoài buổi tối",
            "${sv.nightOutings} ${if (isVi) "đêm/tuần" else "nights/week"}"))
        add(HabitRow("🥗", "Healthy Eating",     "Ăn uống lành mạnh",
            scaleDisplay(sv.healthyEating)))
        add(HabitRow("📱", "Social Media",       "Mạng xã hội",
            scaleDisplay(sv.socialMedia)))
        add(HabitRow("🎯", "Goal Setting",       "Đặt mục tiêu",
            scaleDisplay(sv.goalSetting)))
        add(HabitRow("🎭", "Mood Swings",        "Thay đổi tâm trạng",
            scaleDisplay(sv.moodSwings)))
    }

    Text(
        text = if (isVi) "Hồ sơ thói quen" else "Habit Profile",
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.Gray,
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            rows.forEachIndexed { i, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(row.emoji, fontSize = 16.sp, modifier = Modifier.width(28.dp))
                    Text(
                        text = if (isVi) row.labelVi else row.labelEn,
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f),
                    )
                    Text(
                        text = row.display,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = green800,
                    )
                }
                if (i < rows.lastIndex) {
                    HorizontalDivider(
                        color = Color(0xFFF5F5F5),
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EcoStatCard(
    icon: String,
    countLabel: String,
    ratioValue: Int,
    ratioText: String,
    ratioSubtitle: String,
    bg: Color,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(icon, fontSize = 20.sp)
                Text(countLabel, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.DarkGray)
            }
            Text(ratioText, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = accent)
            LinearProgressIndicator(
                progress = { (ratioValue / 100f).coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(5.dp)),
                color = accent,
                trackColor = accent.copy(alpha = 0.15f),
            )
            Text(ratioSubtitle, fontSize = 10.sp, color = Color.Gray)
        }
    }
}
