package com.vodang.greenmind.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.components.AppScaffold
import com.vodang.greenmind.home.components.BottomNavTab
import com.vodang.greenmind.home.components.CollectorDashboard
import com.vodang.greenmind.home.components.HouseholdDashboard
import com.vodang.greenmind.home.components.LanguagePickerModal
import com.vodang.greenmind.home.components.LanguageSwitcher
import com.vodang.greenmind.home.components.ProfilePlaceholder
import com.vodang.greenmind.home.components.UserType
import com.vodang.greenmind.home.components.UserTypePickerModal
import com.vodang.greenmind.home.components.VolunteerDashboard
import com.vodang.greenmind.home.components.WalkDistanceScreen
import com.vodang.greenmind.home.components.CampaignsScreen
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.permission.PermissionGroup
import com.vodang.greenmind.permission.PermissionRequester
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.store.WalkDistanceStore
import com.vodang.greenmind.ProfileScreen
import com.vodang.greenmind.SettingsScreen
import com.vodang.greenmind.bill.BillScreen
import com.vodang.greenmind.catalogue.CatalogueScreen
import com.vodang.greenmind.energy.EnergyScreen
import com.vodang.greenmind.meal.MealScreen
import com.vodang.greenmind.wastereport.WasteReportScreen
import com.vodang.greenmind.wasteimpact.WasteImpactScreen
import com.vodang.greenmind.wastesort.WasteTotalMassScreen
import com.vodang.greenmind.householdwaste.HouseholdWasteScreen
import com.vodang.greenmind.wasteanalytics.WasteAnalyticsScreen
import com.vodang.greenmind.wastesort.WasteSortScreen
import com.vodang.greenmind.todos.TodoScreen
import com.vodang.greenmind.blog.BlogScreen
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.preappsurvey.PreAppSurveyScreen
import kotlinx.coroutines.launch

private val green800 = Color(0xFF2E7D32)
private val green50 = Color(0xFFE8F5E9)
private val green100 = Color(0xFFC8E6C9)
private val textSecondary = Color(0xFF757575)

enum class DetailDest { WASTE_SORT, WASTE_REPORT, WASTE_IMPACT, HOUSEHOLD_WASTE, WASTE_ANALYTICS, MEAL, BILL, ENERGY, PROFILE, SETTINGS, CATALOGUE, PRE_APP_SURVEY, CAMPAIGNS, WASTE_TOTAL_MASS, WALK_DISTANCE, ENVIRONMENTAL_IMPACT }

data class FeatureItem(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val onClick: () -> Unit
)

@Composable
private fun FeatureCatalogueDrawer(
    onDismiss: () -> Unit,
    onFeatureClick: (DetailDest?) -> Unit,
    currentDest: DetailDest?,
    modifier: Modifier = Modifier
) {
    val s = LocalAppStrings.current
    var searchQuery by remember { mutableStateOf("") }

    val allFeatures = listOf(
        FeatureItem(Icons.Filled.QrCodeScanner, s.wasteSort, { onFeatureClick(DetailDest.WASTE_SORT) }),
        FeatureItem(Icons.Filled.Delete, s.wasteReport, { onFeatureClick(DetailDest.WASTE_REPORT) }),
        FeatureItem(Icons.Filled.Analytics, s.wasteStatTitle, { onFeatureClick(DetailDest.WASTE_ANALYTICS) }),
        FeatureItem(Icons.Filled.Scale, s.wasteTotalMassTitle, { onFeatureClick(DetailDest.WASTE_TOTAL_MASS) }),
        FeatureItem(Icons.Filled.Restaurant, s.scanMeal, { onFeatureClick(DetailDest.MEAL) }),
        FeatureItem(Icons.Filled.Receipt, s.billScreenTitle, { onFeatureClick(DetailDest.BILL) }),
        FeatureItem(Icons.Filled.House, s.household, { onFeatureClick(DetailDest.HOUSEHOLD_WASTE) }),
        FeatureItem(Icons.Filled.ElectricBolt, s.electricityUsage, { onFeatureClick(DetailDest.ENERGY) }),
        FeatureItem(Icons.Filled.DirectionsWalk, s.walkDistance, { onFeatureClick(DetailDest.WALK_DISTANCE) }),
        FeatureItem(Icons.Filled.Eco, s.environmentalImpact, { onFeatureClick(DetailDest.ENVIRONMENTAL_IMPACT) }),
        FeatureItem(Icons.Filled.Groups, s.blog, { onFeatureClick(null) }),
        FeatureItem(Icons.Filled.Campaign, s.campaignsTitle, { onFeatureClick(DetailDest.CAMPAIGNS) }),
        FeatureItem(Icons.Filled.CheckCircle, s.todos, { onFeatureClick(null) }),
        FeatureItem(Icons.Filled.Assignment, s.surveys, { onFeatureClick(null) }),
        FeatureItem(Icons.Filled.Person, s.profileTitle, { onFeatureClick(DetailDest.PROFILE) }),
        FeatureItem(Icons.Filled.Settings, s.settings, { onFeatureClick(DetailDest.SETTINGS) }),
        FeatureItem(Icons.Filled.BugReport, s.editProfile, { onFeatureClick(DetailDest.CATALOGUE) }),
    )

    val filteredFeatures = if (searchQuery.isBlank()) {
        allFeatures
    } else {
        allFeatures.filter { it.label.contains(searchQuery, ignoreCase = true) }
    }

    Surface(
        modifier = modifier.fillMaxHeight(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(green50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Apps,
                            contentDescription = null,
                            tint = green800,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = s.catalogue,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = green800
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF5F5F5))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                placeholder = { Text(s.blogSearchHint, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "Clear",
                                tint = textSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = green800,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = green50.copy(alpha = 0.3f),
                    unfocusedContainerColor = Color(0xFFF5F5F5)
                ),
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
            )

            HorizontalDivider(color = Color(0xFFE0E0E0))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                filteredFeatures.forEach { feature ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                feature.onClick()
                                onDismiss()
                            }
                            .padding(horizontal = 12.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(green50),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = null,
                                tint = green800,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = feature.label,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color.DarkGray,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = textSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                if (filteredFeatures.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.SearchOff,
                                contentDescription = null,
                                tint = textSecondary,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No features found",
                                fontSize = 14.sp,
                                color = textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun HomeScreen(
    onLogout: () -> Unit = {},
    hasPreAppSurvey: Boolean = true,
    onPreAppSurveyCompleted: () -> Unit = {},
) {
    val s = LocalAppStrings.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val user by SettingsStore.user.collectAsState()
    val language by SettingsStore.language.collectAsState()
    val roleSwitchEnabled by SettingsStore.roleSwitcherEnabled.collectAsState()
    val household by HouseholdStore.household.collectAsState()
    var userType by remember { mutableStateOf(UserType.HOUSEHOLD) }
    var bottomNavTab by remember { mutableStateOf(BottomNavTab.HOME) }
    val dashboardScrollState = rememberScrollState()
    val collectorScrollState = rememberScrollState()
    val volunteerScrollState = rememberScrollState()
    var showPicker by remember { mutableStateOf(false) }
    var showLangPicker by remember { mutableStateOf(false) }
    var detailDest by remember { mutableStateOf<DetailDest?>(null) }
    var showRightDrawer by remember { mutableStateOf(false) }
    var dragOffsetX by remember { mutableFloatStateOf(0f) }

    BackHandler(enabled = detailDest != null) { detailDest = null }

    val locationGranted by PermissionRequester.grantedFlow(PermissionGroup.LOCATION).collectAsState()
    LaunchedEffect(Unit) {
        if (!PermissionRequester.grantedFlow(PermissionGroup.LOCATION).value) {
            PermissionRequester.request(PermissionGroup.LOCATION)
        }
        HouseholdStore.fetchHousehold()
    }
    LaunchedEffect(locationGranted) {
        if (locationGranted) Geo.service.start()
    }
    LaunchedEffect(Unit) {
        WalkDistanceStore.startPolling()
    }

    val activeScrollState = when {
        userType == UserType.COLLECTOR -> collectorScrollState
        userType == UserType.VOLUNTEER -> volunteerScrollState
        else -> dashboardScrollState
    }
    val topBarScrolled = detailDest != null ||
        activeScrollState?.value?.let { it > 0 } ?: false

    val trailingIconsForTab: @Composable (RowScope.() -> Unit) = {
        when (bottomNavTab) {
            BottomNavTab.HOME -> {
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = green800, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = green800, modifier = Modifier.size(20.dp))
                }
            }
            BottomNavTab.WASTE_SCAN -> {
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.CloudUpload, contentDescription = "Upload", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = green800, modifier = Modifier.size(22.dp))
                }
            }
            BottomNavTab.TODOS -> {
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = green800, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = green800, modifier = Modifier.size(20.dp))
                }
            }
            BottomNavTab.HOUSEHOLD -> {
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = green800, modifier = Modifier.size(22.dp))
                }
            }
            BottomNavTab.BLOG -> {
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Campaign, contentDescription = "Campaign", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "New Post", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Leaderboard, contentDescription = "Leaderboard", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = green800, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat", tint = green800, modifier = Modifier.size(20.dp))
                }
            }
            BottomNavTab.PROFILE -> {
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = green800, modifier = Modifier.size(22.dp))
                }
                IconButton(onClick = { /* TODO */ }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.BugReport, contentDescription = "Dev Settings", tint = green800, modifier = Modifier.size(22.dp))
                }
                if (roleSwitchEnabled) {
                    IconButton(onClick = { showPicker = true }, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = when (userType) {
                                UserType.HOUSEHOLD -> Icons.Filled.House
                                UserType.COLLECTOR -> Icons.Filled.LocalShipping
                                UserType.VOLUNTEER -> Icons.Filled.VolunteerActivism
                            },
                            contentDescription = "Switch Role",
                            tint = green800,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            gesturesEnabled = detailDest == null && userType != UserType.COLLECTOR,
            drawerContent = {
                Surface(
                    modifier = Modifier.fillMaxHeight(),
                    color = Color.White,
                ) {
                    BoxWithConstraints(
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        val isSmallDrawer = maxHeight < 520.dp
                        val itemPad = if (isSmallDrawer) 6.dp else 8.dp
                        val dividerPad = if (isSmallDrawer) 2.dp else 4.dp
                        val closeBtnPad = if (isSmallDrawer) 2.dp else 4.dp

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(horizontal = 12.dp),
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(top = closeBtnPad, end = closeBtnPad),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF5F5F5))
                                        .clickable { scope.launch { drawerState.close() } },
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(Icons.Filled.Close, contentDescription = null, tint = Color(0xFF757575), modifier = Modifier.size(18.dp))
                                }
                            }
                            ProfilePlaceholder(
                                user = user,
                                onEditClick = {
                                    detailDest = DetailDest.PROFILE
                                    scope.launch { drawerState.close() }
                                },
                                compact = isSmallDrawer,
                            )
                            HorizontalDivider()
                            Spacer(Modifier.height(dividerPad))
                            DrawerItem(label = s.home, onClick = {
                                detailDest = null
                                scope.launch { drawerState.close() }
                            }, pad = itemPad)
                            DrawerItem(label = s.todos, onClick = {
                                bottomNavTab = BottomNavTab.TODOS
                                scope.launch { drawerState.close() }
                            }, pad = itemPad)
                            DrawerItem(label = s.surveys, onClick = {
                                scope.launch { drawerState.close() }
                            }, pad = itemPad)
                            DrawerItem(label = s.blog, onClick = {
                                bottomNavTab = BottomNavTab.BLOG
                                scope.launch { drawerState.close() }
                            }, pad = itemPad)
                            DrawerItem(label = s.catalogue, onClick = {
                                scope.launch { drawerState.close() }
                                showRightDrawer = true
                            }, pad = itemPad)
                            DrawerItem(label = s.settings, onClick = {
                                detailDest = DetailDest.SETTINGS
                                scope.launch { drawerState.close() }
                            }, pad = itemPad)
                            Spacer(Modifier.height(dividerPad))
                            HorizontalDivider()
                            Spacer(Modifier.height(dividerPad))
                            LanguageSwitcher(
                                currentLang = language,
                                onClick = { showLangPicker = true }
                            )
                            HorizontalDivider()
                            Spacer(Modifier.height(dividerPad))
                            Row(
                                modifier = Modifier.padding(vertical = itemPad).clickable {
                                    scope.launch { drawerState.close() }
                                    SettingsStore.clearAll()
                                    onLogout()
                                },
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Logout, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                                Text(text = s.logout, color = Color(0xFFC62828))
                            }
                            Spacer(Modifier.height(dividerPad))
                        }
                    }
                }
            },
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = { offset ->
                                    dragOffsetX = 0f
                                },
                                onDragEnd = {
                                    if (dragOffsetX < -100f && detailDest == null) {
                                        showRightDrawer = true
                                    }
                                    dragOffsetX = 0f
                                },
                                onDragCancel = {
                                    dragOffsetX = 0f
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    dragOffsetX += dragAmount
                                }
                            )
                        }
                ) {
                    AppScaffold(
                        title = when (bottomNavTab) {
                            BottomNavTab.HOME -> "GreenMind"
                            BottomNavTab.HOUSEHOLD -> household?.address ?: s.household
                            else -> bottomNavTab.label(s)
                        },
                        subtitle = when (bottomNavTab) {
                            BottomNavTab.HOME -> s.welcomeBack
                            BottomNavTab.HOUSEHOLD -> household?.members?.size?.let { s.memberCount(it) }
                            else -> null
                        },
                        onMenuClick = { scope.launch { drawerState.open() } },
                        userType = userType,
                        showRoleSwitcher = bottomNavTab == BottomNavTab.PROFILE,
                        onSwitchClick = { showPicker = true },
                        selectedTab = bottomNavTab,
                        onTabSelected = { bottomNavTab = it },
                        scrolled = topBarScrolled,
                        trailingIcons = trailingIconsForTab,
                    ) {
                        when (detailDest) {
                            DetailDest.WASTE_SORT -> WasteSortScreen(
                                onScanClick = { detailDest = null },
                            )
                            DetailDest.WASTE_REPORT -> WasteReportScreen()
                            DetailDest.WASTE_IMPACT -> WasteImpactScreen()
                            DetailDest.HOUSEHOLD_WASTE -> HouseholdWasteScreen(
                                onBack = { detailDest = null },
                                onNavigateToWasteImpact = { detailDest = DetailDest.WASTE_IMPACT },
                            )
                            DetailDest.WASTE_ANALYTICS -> WasteAnalyticsScreen()
                            DetailDest.MEAL -> MealScreen()
                            DetailDest.BILL -> BillScreen()
                            DetailDest.ENERGY -> EnergyScreen()
                            DetailDest.PROFILE -> ProfileScreen()
                            DetailDest.SETTINGS -> SettingsScreen()
                            DetailDest.CATALOGUE -> CatalogueScreen(
                                onWasteReport = { detailDest = DetailDest.WASTE_REPORT },
                                onPreAppSurvey = { detailDest = DetailDest.PRE_APP_SURVEY },
                            )
                            DetailDest.PRE_APP_SURVEY -> PreAppSurveyScreen(
                                onCompleted = {
                                    onPreAppSurveyCompleted()
                                    detailDest = null
                                }
                            )
                            DetailDest.CAMPAIGNS -> CampaignsScreen(
                                onBack = { detailDest = null },
                            )
                            DetailDest.WASTE_TOTAL_MASS -> WasteTotalMassScreen(
                                onBack = { detailDest = null },
                            )
                            DetailDest.WALK_DISTANCE -> WalkDistanceScreen(
                                onBack = { detailDest = null },
                            )
                            DetailDest.ENVIRONMENTAL_IMPACT -> WasteImpactScreen()
                            null -> {
                                when (bottomNavTab) {
                                    BottomNavTab.HOME -> {
                                        when (userType) {
                                            UserType.HOUSEHOLD -> HouseholdDashboard(
                                                scrollState = dashboardScrollState,
                                                onWasteSortClick = { detailDest = DetailDest.WASTE_SORT },
                                                onWasteImpactClick = { detailDest = DetailDest.WASTE_IMPACT },
                                                onHouseholdWasteClick = { detailDest = DetailDest.HOUSEHOLD_WASTE },
                                                onWasteStatClick = { detailDest = DetailDest.WASTE_ANALYTICS },
                                                onWasteReportClick = { detailDest = DetailDest.WASTE_REPORT },
                                                onWasteTotalMassClick = { detailDest = DetailDest.WASTE_TOTAL_MASS },
                                                onScanMealClick = { detailDest = DetailDest.MEAL },
                                                onScanBillClick = { detailDest = DetailDest.BILL },
                                                onElectricityClick = { detailDest = DetailDest.ENERGY },
                                                onWalkDistanceClick = { detailDest = DetailDest.WALK_DISTANCE },
                                                onEnvironmentalImpactClick = { detailDest = DetailDest.ENVIRONMENTAL_IMPACT },
                                                onBlogClick = { bottomNavTab = BottomNavTab.BLOG },
                                                onCampaignsClick = { detailDest = DetailDest.CAMPAIGNS },
                                                showPreAppSurveyBadge = !hasPreAppSurvey,
                                                onPreAppSurveyClick = { detailDest = DetailDest.PRE_APP_SURVEY },
                                            )
                                            UserType.COLLECTOR -> CollectorDashboard(user = user, scrollState = collectorScrollState)
                                            UserType.VOLUNTEER -> VolunteerDashboard(user = user, scrollState = volunteerScrollState)
                                        }
                                    }
                                    BottomNavTab.WASTE_SCAN -> WasteSortScreen(onScanClick = { bottomNavTab = BottomNavTab.HOME })
                                    BottomNavTab.TODOS -> TodoScreen()
                                    BottomNavTab.HOUSEHOLD -> HouseholdWasteScreen(
                                        onBack = { bottomNavTab = BottomNavTab.HOME },
                                        onNavigateToWasteImpact = { detailDest = DetailDest.WASTE_IMPACT },
                                    )
                                    BottomNavTab.BLOG -> BlogScreen()
                                    BottomNavTab.PROFILE -> ProfileScreen()
                                }
                            }
                        }

                        if (showPicker) {
                            UserTypePickerModal(
                                current = userType,
                                onSelect = { userType = it; detailDest = null },
                                onDismiss = { showPicker = false }
                            )
                        }
                        if (showLangPicker) {
                            LanguagePickerModal(
                                currentLang = language,
                                onSelect = { SettingsStore.setLanguage(it) },
                                onDismiss = { showLangPicker = false }
                            )
                        }
                    }
                }
            }
        )

        // Scrim for right drawer (behind the drawer)
        if (showRightDrawer) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showRightDrawer = false }
            )
        }

        // Right Drawer - Feature Catalogue
        AnimatedVisibility(
            visible = showRightDrawer,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            FeatureCatalogueDrawer(
                onDismiss = { showRightDrawer = false },
                onFeatureClick = { dest ->
                    detailDest = dest
                    showRightDrawer = false
                },
                currentDest = detailDest,
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
            )
        }
    }
}

@Composable
private fun DrawerItem(label: String, onClick: () -> Unit, pad: androidx.compose.ui.unit.Dp) {
    Text(
        text = label,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = pad),
        fontSize = 14.sp,
    )
}
