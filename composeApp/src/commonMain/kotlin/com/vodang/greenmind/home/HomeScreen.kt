package com.vodang.greenmind.home

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.ProfileScreen
import com.vodang.greenmind.SettingsScreen
import com.vodang.greenmind.bill.BillScreen
import com.vodang.greenmind.catalogue.CatalogueScreen
import com.vodang.greenmind.energy.EnergyScreen
import com.vodang.greenmind.meal.MealScreen
import com.vodang.greenmind.survey.SurveyScreen
import com.vodang.greenmind.todos.TodoScreen
import com.vodang.greenmind.wastesort.WasteSortScreen
import com.vodang.greenmind.platform.BackHandler
import kotlinx.coroutines.launch

enum class HomeDestination { WASTE_SORT, DASHBOARD, TODOS, SURVEYS }

@Composable
fun HomeScreen(onLogout: () -> Unit = {}) {
    val s = LocalAppStrings.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val user by SettingsStore.user.collectAsState()
    val language by SettingsStore.language.collectAsState()
    var userType by remember { mutableStateOf(UserType.HOUSEHOLD) }
    val dashboardScrollState  = rememberScrollState()
    val collectorScrollState  = rememberScrollState()
    val volunteerScrollState  = rememberScrollState()
    var showPicker by remember { mutableStateOf(false) }
    var showLangPicker by remember { mutableStateOf(false) }
    var showMealScreen by remember { mutableStateOf(false) }
    var showBillScreen by remember { mutableStateOf(false) }
    var showCatalogueScreen by remember { mutableStateOf(false) }
    var showEnergyScreen    by remember { mutableStateOf(false) }
    var showProfileScreen   by remember { mutableStateOf(false) }
    var showSettingsScreen  by remember { mutableStateOf(false) }

    BackHandler(enabled = showMealScreen)      { showMealScreen = false }
    BackHandler(enabled = showBillScreen)      { showBillScreen = false }
    BackHandler(enabled = showCatalogueScreen) { showCatalogueScreen = false }
    BackHandler(enabled = showEnergyScreen)    { showEnergyScreen = false }
    BackHandler(enabled = showProfileScreen)   { showProfileScreen = false }
    BackHandler(enabled = showSettingsScreen)  { showSettingsScreen = false }

    if (showMealScreen) {
        MealScreen(onBack = { showMealScreen = false })
        return
    }
    if (showBillScreen) {
        BillScreen(onBack = { showBillScreen = false })
        return
    }
    if (showCatalogueScreen) {
        CatalogueScreen(onBack = { showCatalogueScreen = false })
        return
    }
    if (showEnergyScreen) {
        EnergyScreen(onBack = { showEnergyScreen = false })
        return
    }
    if (showProfileScreen) {
        ProfileScreen(onBack = { showProfileScreen = false })
        return
    }
    if (showSettingsScreen) {
        SettingsScreen(onBack = { showSettingsScreen = false })
        return
    }

    LaunchedEffect(Unit) { Geo.service.start() }

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
    // Always opaque on non-dashboard pages; on dashboard, driven by scroll position.
    val topBarScrolled = activeScrollState?.value?.let { it > 0 } ?: (destination != HomeDestination.DASHBOARD)

    // Drawer nav → pager
    val navigateTo: (HomeDestination) -> Unit = { dest ->
        scope.launch { pagerState.animateScrollToPage(destinations.indexOf(dest)) }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        // Disable swipe-to-open when a map is on screen — map gestures must take priority.
        gesturesEnabled = destination == HomeDestination.DASHBOARD && userType != UserType.COLLECTOR,
        drawerContent = {
            ModalDrawerSheet {
                Column(modifier = Modifier.fillMaxHeight().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, end = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5F5F5))
                                .clickable { scope.launch { drawerState.close() } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", fontSize = 14.sp, color = Color(0xFF757575))
                        }
                    }
                    ProfilePlaceholder(user = user, onEditClick = {
                        showProfileScreen = true
                        scope.launch { drawerState.close() }
                    })
                    HorizontalDivider()
                    Spacer(Modifier.height(4.dp))
                    Text(s.home, modifier = Modifier.padding(8.dp).clickable {
                        navigateTo(HomeDestination.DASHBOARD)
                        scope.launch { drawerState.close() }
                    })
                    Text(s.todos, modifier = Modifier.padding(8.dp).clickable {
                        navigateTo(HomeDestination.TODOS)
                        scope.launch { drawerState.close() }
                    })
                    Text(s.surveys, modifier = Modifier.padding(8.dp).clickable {
                        navigateTo(HomeDestination.SURVEYS)
                        scope.launch { drawerState.close() }
                    })
                    Text(s.catalogue, modifier = Modifier.padding(8.dp).clickable {
                        showCatalogueScreen = true
                        scope.launch { drawerState.close() }
                    })
                    Text(s.settings, modifier = Modifier.padding(8.dp).clickable {
                        showSettingsScreen = true
                        scope.launch { drawerState.close() }
                    })
                    Spacer(Modifier.weight(1f))
                    HorizontalDivider()
                    LanguageSwitcher(
                        currentLang = language,
                        onClick = { showLangPicker = true }
                    )
                    HorizontalDivider()
                    Text(
                        text = "🚪  ${s.logout}",
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(8.dp).clickable {
                            scope.launch { drawerState.close() }
                            SettingsStore.clearAll()
                            onLogout()
                        }
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                HomeTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onCameraClick = { /*TODO*/ },
                    user = user,
                    userType = userType,
                    onSwitchClick = { showPicker = true },
                    scrolled = topBarScrolled,
                )
            },
            containerColor = Color.Transparent,
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                userScrollEnabled = true
            ) { page ->
                when (destinations[page]) {
                    HomeDestination.DASHBOARD -> when (userType) {
                        UserType.HOUSEHOLD -> HouseholdDashboard(
                            scrollState = dashboardScrollState,
                            onScanMealClick = { showMealScreen = true },
                            onScanBillClick = { showBillScreen = true },
                            onWasteSortClick = { navigateTo(HomeDestination.WASTE_SORT) },
                            onTodosClick = { navigateTo(HomeDestination.TODOS) },
                            onElectricityClick = { showEnergyScreen = true },
                        )
                        UserType.COLLECTOR -> CollectorDashboard(user = user, scrollState = collectorScrollState)
                        UserType.VOLUNTEER -> VolunteerDashboard(user = user, scrollState = volunteerScrollState)
                    }
                    HomeDestination.WASTE_SORT -> WasteSortScreen()
                    HomeDestination.TODOS -> TodoScreen()
                    HomeDestination.SURVEYS -> SurveyScreen()
                }
            }

            if (showPicker) {
                UserTypePickerModal(
                    current = userType,
                    onSelect = { userType = it },
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
