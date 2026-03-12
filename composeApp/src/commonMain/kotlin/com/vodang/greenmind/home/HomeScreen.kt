package com.vodang.greenmind.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.CameraScreen
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.location.Location
import com.vodang.greenmind.permission.PermissionGroup
import com.vodang.greenmind.permission.PermissionRequester
import kotlinx.coroutines.launch

// ── Nav items ──────────────────────────────────────────────────────────────────
enum class NavSection(val label: String, val icon: String) {
    DASHBOARD("Dashboard",        "🏠"),
    ECO_ACTIVITIES("Eco Activities",  "🌿"),
    CAMPAIGNS("Campaigns",         "📢"),
    WASTE_SORTING("Waste Sorting AI", "♻️"),
    POLLUTION("Pollution Reports",  "🌫️"),
    BLOG("Blog",                "📝"),
    MAP("Map",                  "🗺️"),
    PROFILE("Profile",             "👤"),
}

private val green800 = Color(0xFF2E7D32)
private val green50  = Color(0xFFE8F5E9)
private val green100 = Color(0xFFC8E6C9)

// ── Root composable ─────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var currentSection by remember { mutableStateOf(NavSection.DASHBOARD) }
    var showSettings by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            SidebarContent(
                currentSection = currentSection,
                onSelect = { section ->
                    currentSection = section
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopBar(
                    onMenuClick    = { scope.launch { drawerState.open() } },
                    onSettingsClick = { showSettings = !showSettings },
                    showSettings   = showSettings,
                    onSettingsDismiss = { showSettings = false }
                )
            },
            containerColor = Color(0xFFF9FBF9)
        ) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                SectionContent(section = currentSection)
            }
        }
    }
}

// ── Top bar ─────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onMenuClick: () -> Unit,
    onSettingsClick: () -> Unit,
    showSettings: Boolean,
    onSettingsDismiss: () -> Unit
) {
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(green800, CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("🌱", fontSize = 16.sp) }
                Spacer(Modifier.width(8.dp))
                Text(
                    "GreenMind",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = green800
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Text("☰", fontSize = 20.sp)
            }
        },
        actions = {
            // Notification bell
            IconButton(onClick = { /* TODO */ }) {
                Text("🔔", fontSize = 20.sp)
            }
            // Avatar
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(green100),
                contentAlignment = Alignment.Center
            ) { Text("👤", fontSize = 18.sp) }
            Spacer(Modifier.width(4.dp))
            // Settings gear with dropdown
            Box {
                IconButton(onClick = onSettingsClick) {
                    Text("⚙️", fontSize = 20.sp)
                }
                DropdownMenu(
                    expanded = showSettings,
                    onDismissRequest = onSettingsDismiss
                ) {
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { onSettingsDismiss() }
                    )
                    DropdownMenuItem(
                        text = { Text("Help") },
                        onClick = { onSettingsDismiss() }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Log out", color = Color.Red) },
                        onClick = { onSettingsDismiss() }
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.White
        )
    )
}

// ── Sidebar / Drawer ─────────────────────────────────────────────────────────────
@Composable
private fun SidebarContent(
    currentSection: NavSection,
    onSelect: (NavSection) -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        modifier = Modifier.widthIn(max = 280.dp)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(green800)
                .padding(24.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Text("🌱", fontSize = 28.sp) }
                Spacer(Modifier.height(8.dp))
                Text("GreenMind", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Smart Environmental System", fontSize = 11.sp, color = Color.White.copy(alpha = 0.8f))
            }
        }

        Spacer(Modifier.height(8.dp))

        NavSection.entries.forEach { section ->
            val selected = section == currentSection
            NavigationDrawerItem(
                label = {
                    Text(
                        section.label,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                    )
                },
                icon = { Text(section.icon, fontSize = 18.sp) },
                selected = selected,
                onClick = { onSelect(section) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = green50,
                    selectedTextColor      = green800,
                    selectedIconColor      = green800
                ),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
            )
        }
    }
}

// ── Section content router ──────────────────────────────────────────────────────
@Composable
private fun SectionContent(section: NavSection) {
    when (section) {
        NavSection.DASHBOARD    -> DashboardContent()
        NavSection.MAP          -> CameraScreen()   // replace with MapScreen when ready
        NavSection.PROFILE      -> ProfilePlaceholder(section)
        else                    -> PlaceholderContent(section)
    }
}

// ── Dashboard ───────────────────────────────────────────────────────────────────
@Composable
private fun DashboardContent() {
    var running by remember { mutableStateOf(false) }
    val locations = remember { mutableStateListOf<Location>() }
    val granted by PermissionRequester.grantedFlow(PermissionGroup.LOCATION).collectAsState()

    LaunchedEffect(running) { if (!running) Geo.service.stop() }
    LaunchedEffect(granted, running) {
        if (running && granted) Geo.service.start()
        else if (running && !granted) PermissionRequester.request(PermissionGroup.LOCATION)
    }
    LaunchedEffect(Unit) {
        Geo.service.locationUpdates.collect { loc ->
            locations.add(0, loc)
            if (locations.size > 200) locations.removeLast()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Dashboard", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = green800)
        Spacer(Modifier.height(16.dp))

        // Stats strip
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard("📍", "Location", if (running) "Active" else "Idle", Modifier.weight(1f))
            StatCard("🌡️", "Air Quality", "Good", Modifier.weight(1f))
            StatCard("♻️", "Recycled", "12 kg", Modifier.weight(1f))
        }

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = { running = !running },
            colors = ButtonDefaults.buttonColors(containerColor = green800)
        ) { Text(if (running) "Stop tracking" else "Start tracking") }

        AnimatedVisibility(visible = locations.isNotEmpty()) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(Modifier.height(12.dp))
                val latest = locations.firstOrNull()
                if (latest != null) {
                    SectionCard {
                        Text("Latest Location", fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Lat: ${latest.latitude}")
                        Text("Lon: ${latest.longitude}")
                        Text("Accuracy: ${latest.accuracy} m")
                    }
                }
                Spacer(Modifier.height(12.dp))
                Text("History", fontWeight = FontWeight.SemiBold, color = green800)
                Spacer(Modifier.height(4.dp))
                LazyColumn {
                    items(locations) { l ->
                        Text(
                            "lat=${l.latitude}, lon=${l.longitude}, acc=${l.accuracy}m",
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ── Profile ─────────────────────────────────────────────────────────────────────
@Composable
private fun ProfilePlaceholder(section: NavSection) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier.size(96.dp).background(green100, CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("👤", fontSize = 48.sp) }
        Spacer(Modifier.height(16.dp))
        Text("User Name", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("user@example.com", fontSize = 14.sp, color = Color.Gray)
        Spacer(Modifier.height(32.dp))
        SectionCard {
            Text("Edit Profile", fontWeight = FontWeight.Medium)
        }
    }
}

// ── Generic placeholder ──────────────────────────────────────────────────────────
@Composable
private fun PlaceholderContent(section: NavSection) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(section.icon, fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(section.label, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = green800)
            Spacer(Modifier.height(8.dp))
            Text("Coming soon", fontSize = 14.sp, color = Color.Gray)
        }
    }
}

// ── Shared small composables ────────────────────────────────────────────────────
@Composable
private fun StatCard(icon: String, label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = green800)
            Text(label, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun SectionCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), content = content)
    }
}
