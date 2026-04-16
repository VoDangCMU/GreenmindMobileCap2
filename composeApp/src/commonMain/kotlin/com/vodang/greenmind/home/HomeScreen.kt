package com.vodang.greenmind.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.focusable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.home.components.CollectorDashboard
import com.vodang.greenmind.home.components.HomeTopBar
import com.vodang.greenmind.home.components.HouseholdDashboard
import com.vodang.greenmind.home.components.LanguagePickerModal
import com.vodang.greenmind.home.components.LanguageSwitcher
import com.vodang.greenmind.home.components.ProfilePlaceholder
import com.vodang.greenmind.home.components.UserType
import com.vodang.greenmind.home.components.UserTypePickerModal
import com.vodang.greenmind.home.components.VolunteerDashboard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.location.Geo
import com.vodang.greenmind.permission.PermissionGroup
import com.vodang.greenmind.permission.PermissionRequester
import com.vodang.greenmind.store.HouseholdStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.ProfileScreen
import com.vodang.greenmind.SettingsScreen
import com.vodang.greenmind.bill.BillScreen
import com.vodang.greenmind.catalogue.CatalogueScreen
import com.vodang.greenmind.energy.EnergyScreen
import com.vodang.greenmind.meal.MealScreen
import com.vodang.greenmind.blog.BlogScreen
import com.vodang.greenmind.survey.SurveyScreen
import com.vodang.greenmind.todos.TodoScreen
import com.vodang.greenmind.wastereport.WasteReportScreen
import com.vodang.greenmind.wasteimpact.WasteImpactScreen
import com.vodang.greenmind.householdwaste.HouseholdWasteScreen
import com.vodang.greenmind.wasteanalytics.WasteAnalyticsScreen
import com.vodang.greenmind.wastesort.WasteSortScreen
import com.vodang.greenmind.platform.BackHandler
import com.vodang.greenmind.preappsurvey.PreAppSurveyScreen
import kotlinx.coroutines.launch

enum class HomeDestination { WASTE_SORT, DASHBOARD, TODOS, SURVEYS, BLOG }
enum class DetailDest { WASTE_REPORT, WASTE_IMPACT, HOUSEHOLD_WASTE, WASTE_ANALYTICS, MEAL, BILL, ENERGY, PROFILE, SETTINGS, CATALOGUE, PRE_APP_SURVEY }

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
    var userType by remember { mutableStateOf(UserType.HOUSEHOLD) }
    val dashboardScrollState  = rememberScrollState()
    val collectorScrollState  = rememberScrollState()
    val volunteerScrollState  = rememberScrollState()
    var showPicker by remember { mutableStateOf(false) }
    var showLangPicker by remember { mutableStateOf(false) }
    var detailDest by remember { mutableStateOf<DetailDest?>(null) }

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

    val destinations = HomeDestination.entries
    val pagerState = rememberPagerState(
        initialPage = destinations.indexOf(HomeDestination.DASHBOARD),
        pageCount = { destinations.size }
    )
    val destination = destinations[pagerState.currentPage]
    val activeScrollState = when {
        destination != HomeDestination.DASHBOARD -> null
        userType == UserType.COLLECTOR           -> collectorScrollState
        userType == UserType.VOLUNTEER           -> volunteerScrollState
        else                                     -> dashboardScrollState
    }
    // Always opaque on detail screens; on dashboard pager, driven by scroll position.
    val topBarScrolled = detailDest != null ||
        activeScrollState?.value?.let { it > 0 } ?: (destination != HomeDestination.DASHBOARD)

    // Drawer nav → pager
    val navigateTo: (HomeDestination) -> Unit = { dest ->
        scope.launch { pagerState.animateScrollToPage(destinations.indexOf(dest)) }
    }

    BackHandler(enabled = detailDest == null && destination != HomeDestination.DASHBOARD) {
        navigateTo(HomeDestination.DASHBOARD)
    }

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Disable swipe-to-open on detail screens and when a map is on screen.
        gesturesEnabled = detailDest == null && destination == HomeDestination.DASHBOARD && userType != UserType.COLLECTOR,
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
                            Text("✕", fontSize = 14.sp, color = Color(0xFF757575))
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
                        navigateTo(HomeDestination.DASHBOARD)
                        scope.launch { drawerState.close() }
                    }, pad = itemPad)
                    DrawerItem(label = s.todos, onClick = {
                        navigateTo(HomeDestination.TODOS)
                        scope.launch { drawerState.close() }
                    }, pad = itemPad)
                    DrawerItem(label = s.surveys, onClick = {
                        navigateTo(HomeDestination.SURVEYS)
                        scope.launch { drawerState.close() }
                    }, pad = itemPad)
                    DrawerItem(label = s.blog, onClick = {
                        navigateTo(HomeDestination.BLOG)
                        scope.launch { drawerState.close() }
                    }, pad = itemPad)
                    DrawerItem(label = s.catalogue, onClick = {
                        detailDest = DetailDest.CATALOGUE
                        scope.launch { drawerState.close() }
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
                    Text(
                        text = "🚪  ${s.logout}",
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(vertical = itemPad).clickable {
                            scope.launch { drawerState.close() }
                            SettingsStore.clearAll()
                            onLogout()
                        },
                    )
                    Spacer(Modifier.height(dividerPad))
                }
            }
            }
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    // TODO: Implement quick-scan camera shortcut from the top bar.
                    //       Options: open WasteSortScreen, or a dedicated quick-capture flow.
                    onCameraClick = { /*TODO*/ },
                    user = user,
                    userType = userType,
                    onSwitchClick = { showPicker = true },
                    showRoleSwitcher = roleSwitchEnabled,
                    scrolled = topBarScrolled,
                )
            },
            containerColor = Color.Transparent,
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .focusRequester(focusRequester)
                    .focusable()
                    .onKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                        when (event.key) {
                            Key.DirectionLeft, Key.Escape -> {
                                if (detailDest != null) {
                                    detailDest = null
                                } else if (event.key == Key.DirectionLeft) {
                                    val prev = (pagerState.currentPage - 1).coerceAtLeast(0)
                                    if (prev != pagerState.currentPage)
                                        scope.launch { pagerState.animateScrollToPage(prev) }
                                }
                                true
                            }
                            Key.DirectionRight -> {
                                if (detailDest == null) {
                                    val next = (pagerState.currentPage + 1).coerceAtMost(destinations.size - 1)
                                    if (next != pagerState.currentPage)
                                        scope.launch { pagerState.animateScrollToPage(next) }
                                }
                                true
                            }
                            else -> false
                        }
                    },
            ) {
                when (detailDest) {
                    DetailDest.WASTE_REPORT    -> WasteReportScreen()
                    DetailDest.WASTE_IMPACT    -> WasteImpactScreen()
                    DetailDest.HOUSEHOLD_WASTE  -> HouseholdWasteScreen(
                        onBack = { detailDest = null },
                        onNavigateToWasteImpact = { detailDest = DetailDest.WASTE_IMPACT },
                    )
                    DetailDest.WASTE_ANALYTICS  -> WasteAnalyticsScreen()
                    DetailDest.MEAL         -> MealScreen()
                    DetailDest.BILL         -> BillScreen()
                    DetailDest.ENERGY       -> EnergyScreen()
                    DetailDest.PROFILE      -> ProfileScreen()
                    DetailDest.SETTINGS     -> SettingsScreen()
                    DetailDest.CATALOGUE    -> CatalogueScreen(
                        onWasteReport = { detailDest = DetailDest.WASTE_REPORT },
                        onPreAppSurvey = { detailDest = DetailDest.PRE_APP_SURVEY },
                    )
                    DetailDest.PRE_APP_SURVEY -> PreAppSurveyScreen(
                        onCompleted = {
                            onPreAppSurveyCompleted()
                            detailDest = null
                        }
                    )
                    null -> HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize(),
                        userScrollEnabled = true,
                    ) { page ->
                        when (destinations[page]) {
                            HomeDestination.DASHBOARD -> when (userType) {
                                UserType.HOUSEHOLD -> HouseholdDashboard(
                                    scrollState = dashboardScrollState,
                                    onWasteSortClick = { navigateTo(HomeDestination.WASTE_SORT) },
                                    onWasteImpactClick = { detailDest = DetailDest.WASTE_IMPACT },
                                    onHouseholdWasteClick = { detailDest = DetailDest.HOUSEHOLD_WASTE },
                                    onWasteStatClick = { detailDest = DetailDest.WASTE_ANALYTICS },
                                    onScanMealClick = { detailDest = DetailDest.MEAL },
                                    onScanBillClick = { detailDest = DetailDest.BILL },
                                    onBlogClick = { navigateTo(HomeDestination.BLOG) },
                                    onWasteReportClick = { detailDest = DetailDest.WASTE_REPORT },
                                    showPreAppSurveyBadge = !hasPreAppSurvey,
                                    onPreAppSurveyClick = { detailDest = DetailDest.PRE_APP_SURVEY },
                                )
                                UserType.COLLECTOR -> CollectorDashboard(user = user, scrollState = collectorScrollState)
                                UserType.VOLUNTEER -> VolunteerDashboard(user = user, scrollState = volunteerScrollState)
                            }
                            HomeDestination.WASTE_SORT -> WasteSortScreen()
                            HomeDestination.TODOS      -> TodoScreen()
                            HomeDestination.SURVEYS    -> SurveyScreen()
                            HomeDestination.BLOG       -> BlogScreen()
                        }
                    }
                }

                if (showPicker) {
                    UserTypePickerModal(
                        current = userType,
                        onSelect = { userType = it; detailDest = null; navigateTo(HomeDestination.DASHBOARD) },
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
