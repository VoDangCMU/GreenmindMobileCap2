package com.vodang.greenmind

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

private val Green800 = Color(0xFF2E7D32)
private val Green50 = Color(0xFFE8F5E9)
private val SurfaceContainer = Color(0xFFF8FAFC)
private val OnSurfaceVariant = Color(0xFF49454F)
private val OutlineColor = Color(0xFFCAC4D0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(scrollState: ScrollState? = null) {
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
            // not completed yet
        } catch (_: Throwable) { }
    }

    val localScrollState = scrollState ?: rememberScrollState()

    Scaffold(
        containerColor = SurfaceContainer,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = s.profileTitle,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent,
                ),
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(localScrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Profile Header Card ───────────────────────────────────────────
            ProfileHeaderCard(user = user, s = s)

            // ── Today's Journey ──────────────────────────────────────────────
            JourneyStatsRow(
                distance = formatDistance(distMeters),
                walkTime = s.walkTimeMin(walkTimeMin),
                calories = s.caloriesKcal(calories),
                s = s,
                modifier = Modifier.fillMaxWidth(),
            )

            // ── Personal Information ─────────────────────────────────────────
            PersonalInfoCard(user = user, s = s)

            // ── OCEAN Score ─────────────────────────────────────────────────
            OceanScoreCard()

            // ── Eco Activity Today ────────────────────────────────────────────
            EcoActivityRow(
                mealsCount = todayMeals.size,
                mealsLabel = s.mealsToday(todayMeals.size),
                plantRatio = avgPlant,
                billsCount = todayBills.size,
                billsLabel = s.billsToday(todayBills.size),
                greenRatio = avgGreen,
            )

            // ── Habit Profile ─────────────────────────────────────────────────
            survey?.let { sv ->
                HabitProfileCard(sv = sv, s = s)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ProfileHeaderCard(
    user: com.vodang.greenmind.api.auth.UserDto?,
    s: com.vodang.greenmind.i18n.AppStrings,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Avatar
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = Green800,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = Color.White,
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Name
            Text(
                text = user?.fullName?.ifBlank { s.profileName } ?: s.profileName,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B),
            )

            // Email
            Text(
                text = user?.email ?: s.profileEmail,
                fontSize = 14.sp,
                color = OnSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            // Role chip
            AssistChip(
                onClick = { },
                label = {
                    Text(
                        text = user?.role?.replaceFirstChar { it.uppercase() } ?: s.notSet,
                        fontSize = 13.sp,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = when (user?.role?.lowercase()) {
                            "collector" -> Icons.Filled.LocalShipping
                            "volunteer" -> Icons.Filled.VolunteerActivism
                            else -> Icons.Filled.Home
                        },
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                },
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = Green50,
                    labelColor = Green800,
                    leadingIconContentColor = Green800,
                ),
                border = AssistChipDefaults.assistChipBorder(
                    borderColor = Green800.copy(alpha = 0.3f),
                    enabled = true,
                ),
            )

            // Location
            user?.location?.takeIf { it.isNotBlank() }?.let { location ->
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = OnSurfaceVariant,
                    )
                    Text(
                        text = location,
                        fontSize = 13.sp,
                        color = OnSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyStatsRow(
    distance: String,
    walkTime: String,
    calories: String,
    s: com.vodang.greenmind.i18n.AppStrings,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        JourneyStatItem(
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            iconDescription = s.walkDistance,
            value = distance,
            modifier = Modifier.weight(1f),
        )
        JourneyStatItem(
            icon = Icons.Filled.Timer,
            iconDescription = s.walkTimeLabel,
            value = walkTime,
            modifier = Modifier.weight(1f),
        )
        JourneyStatItem(
            icon = Icons.Filled.LocalFireDepartment,
            iconDescription = s.caloriesLabel,
            value = calories,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun JourneyStatItem(
    icon: ImageVector,
    iconDescription: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Green50),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FilledIconButton(
                onClick = { },
                modifier = Modifier.size(36.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Green800.copy(alpha = 0.15f),
                    contentColor = Green800,
                ),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = iconDescription,
                    modifier = Modifier.size(20.dp),
                )
            }
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Green800,
            )
        }
    }
}

@Composable
private fun PersonalInfoCard(
    user: com.vodang.greenmind.api.auth.UserDto?,
    s: com.vodang.greenmind.i18n.AppStrings,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            PersonalInfoRow(
                icon = Icons.Filled.Person,
                label = s.fullName,
                value = user?.fullName?.ifBlank { s.notSet } ?: s.notSet,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineColor.copy(alpha = 0.5f))
            PersonalInfoRow(
                icon = Icons.Filled.Email,
                label = s.emailAddress,
                value = user?.email ?: s.notSet,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineColor.copy(alpha = 0.5f))
            PersonalInfoRow(
                icon = Icons.Filled.Wc,
                label = s.genderLabel,
                value = user?.gender?.replaceFirstChar { it.uppercase() } ?: s.notSet,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineColor.copy(alpha = 0.5f))
            PersonalInfoRow(
                icon = Icons.Outlined.Cake,
                label = s.ageLabel,
                value = user?.age?.let { "$it" } ?: s.notSet,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineColor.copy(alpha = 0.5f))
            PersonalInfoRow(
                icon = Icons.Filled.LocationOn,
                label = s.locationLabel,
                value = user?.location?.ifBlank { s.notSet } ?: s.notSet,
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = OutlineColor.copy(alpha = 0.5f))
            PersonalInfoRow(
                icon = Icons.Filled.Badge,
                label = s.roleLabel,
                value = user?.role?.replaceFirstChar { it.uppercase() } ?: s.notSet,
            )
        }
    }
}

@Composable
private fun PersonalInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = Green800,
        )
        Text(
            text = label,
            fontSize = 14.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF1B1B1B),
        )
    }
}

@Composable
private fun EcoActivityRow(
    mealsCount: Int,
    mealsLabel: String,
    plantRatio: Int,
    billsCount: Int,
    billsLabel: String,
    greenRatio: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        EcoStatCard(
            icon = Icons.Filled.Restaurant,
            label = mealsLabel,
            value = "$plantRatio%",
            progress = plantRatio / 100f,
            accentColor = Green800,
            modifier = Modifier.weight(1f),
        )
        EcoStatCard(
            icon = Icons.Filled.Receipt,
            label = billsLabel,
            value = "$greenRatio%",
            progress = greenRatio / 100f,
            accentColor = Color(0xFFF57F17),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EcoStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.08f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilledIconButton(
                    onClick = { },
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = accentColor.copy(alpha = 0.15f),
                        contentColor = accentColor,
                    ),
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = OnSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = value,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor,
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.2f),
            )
        }
    }
}

@Composable
private fun HabitProfileCard(
    sv: com.vodang.greenmind.api.preappsurvey.PreAppSurveyDto,
    s: com.vodang.greenmind.i18n.AppStrings,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        val isVi = s.langCode == "vi"
        val rows = listOf(
            HabitItem("💸", "Daily Spending", "Chi tiêu hàng ngày", "${sv.dailySpending} ${if (isVi) "đ/ngày" else "VND/day"}"),
            HabitItem("🚶", "Daily Distance", "Quãng đường hàng ngày", "${sv.dailyDistance} km"),
            HabitItem("📊", "Spending Variation", "Biến động chi tiêu", scaleDisplay(sv.spendingVariation)),
            HabitItem("🛍️", "Brand Trial", "Thử thương hiệu mới", scaleDisplay(sv.brandTrial)),
            HabitItem("📋", "Shopping List", "Mua sắm theo danh sách", scaleDisplay(sv.shoppingList)),
            HabitItem("🗺️", "Explores New Places", "Khám phá nơi mới", scaleDisplay(sv.newPlaces)),
            HabitItem("🚌", "Public Transport", "Giao thông công cộng", scaleDisplay(sv.publicTransport)),
            HabitItem("🗓️", "Stable Schedule", "Lịch trình ổn định", scaleDisplay(sv.stableSchedule)),
            HabitItem("🌙", "Night Outings", "Ra ngoài buổi tối", "${sv.nightOutings} ${if (isVi) "đêm/tuần" else "nights/week"}"),
            HabitItem("🥗", "Healthy Eating", "Ăn uống lành mạnh", scaleDisplay(sv.healthyEating)),
            HabitItem("📱", "Social Media", "Mạng xã hội", scaleDisplay(sv.socialMedia)),
            HabitItem("🎯", "Goal Setting", "Đặt mục tiêu", scaleDisplay(sv.goalSetting)),
            HabitItem("🎭", "Mood Swings", "Thay đổi tâm trạng", scaleDisplay(sv.moodSwings)),
        )

        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            rows.forEachIndexed { i, row ->
                HabitRow(emoji = row.emoji, label = if (isVi) row.labelVi else row.labelEn, value = row.display)
                if (i < rows.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = OutlineColor.copy(alpha = 0.3f),
                    )
                }
            }
        }
    }
}

private data class HabitItem(val emoji: String, val labelEn: String, val labelVi: String, val display: String)

private fun scaleDisplay(value: Int, max: Int = 5): String = "●".repeat(value) + "○".repeat(max - value)

@Composable
private fun HabitRow(emoji: String, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(emoji, fontSize = 18.sp, modifier = Modifier.width(28.dp))
        Text(
            text = label,
            fontSize = 14.sp,
            color = OnSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Green800,
        )
    }
}