package com.vodang.greenmind.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.home.components.BottomNavTab
import com.vodang.greenmind.home.components.UserType
import com.vodang.greenmind.i18n.LocalAppStrings

private val green800 = Color(0xFF2E7D32)
private val green50 = Color(0xFFE8F5E9)
private val textSecondary = Color(0xFF757575)

@Composable
fun AppScaffold(
    title: String,
    subtitle: String? = null,
    onMenuClick: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    userType: UserType = UserType.HOUSEHOLD,
    showRoleSwitcher: Boolean = false,
    showRoleSwitcherIcon: Boolean = false,
    onSwitchClick: (() -> Unit)? = null,
    trailingIcons: @Composable RowScope.() -> Unit = {},
    selectedTab: BottomNavTab = BottomNavTab.HOME,
    onTabSelected: (BottomNavTab) -> Unit = {},
    scrolled: Boolean = false,
    content: @Composable (PaddingValues) -> Unit,
) {
    val s = LocalAppStrings.current
    val bgColor by animateColorAsState(
        targetValue = if (scrolled) Color.White else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "topBarBg"
    )
    val elevation = if (scrolled) 4.dp else 0.dp

    Column(
        modifier = Modifier
            .fillMaxSize()
            .shadow(elevation)
            .background(bgColor)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(green50)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = green800,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(green50)
                        .clickable { onMenuClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(s.menuIcon, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = green800
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }

            trailingIcons()

            if (showRoleSwitcher && onSwitchClick != null) {
                Spacer(Modifier.width(4.dp))
                if (showRoleSwitcherIcon) {
                    IconButton(
                        onClick = { onSwitchClick() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = when (userType) {
                                UserType.HOUSEHOLD -> Icons.Filled.House
                                UserType.COLLECTOR -> Icons.Filled.LocalShipping
                                UserType.VOLUNTEER -> Icons.Filled.VolunteerActivism
                            },
                            contentDescription = "Switch Role",
                            tint = green800,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(green50)
                            .clickable { onSwitchClick() }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (userType) {
                                UserType.HOUSEHOLD -> Icons.Filled.House
                                UserType.COLLECTOR -> Icons.Filled.LocalShipping
                                UserType.VOLUNTEER -> Icons.Filled.VolunteerActivism
                            },
                            contentDescription = null,
                            tint = green800,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(s.roleSwitch, fontSize = 14.sp, color = green800, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE0E0E0))

        // Navigation Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavTab.entries.forEach { tab ->
                val selected = tab == selectedTab
                IconButton(
                    onClick = { onTabSelected(tab) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = when (tab) {
                            BottomNavTab.HOME -> if (selected) Icons.Filled.Home else Icons.Outlined.Home
                            BottomNavTab.WASTE_SCAN -> if (selected) Icons.Filled.QrCodeScanner else Icons.Outlined.QrCodeScanner
                            BottomNavTab.TODOS -> if (selected) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle
                            BottomNavTab.HOUSEHOLD -> if (selected) Icons.Filled.House else Icons.Outlined.House
                            BottomNavTab.BLOG -> if (selected) Icons.Filled.Article else Icons.Outlined.Article
                            BottomNavTab.PROFILE -> if (selected) Icons.Filled.Person else Icons.Outlined.Person
                        },
                        contentDescription = tab.label(s),
                        tint = if (selected) green800 else textSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            content(PaddingValues())
        }
    }
}
