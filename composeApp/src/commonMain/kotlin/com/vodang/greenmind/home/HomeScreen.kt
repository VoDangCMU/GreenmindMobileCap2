package com.vodang.greenmind.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.survey.SurveyScreen
import com.vodang.greenmind.todos.TodoScreen
import kotlinx.coroutines.launch

enum class HomeDestination { DASHBOARD, TODOS, SURVEYS }

@Composable
fun HomeScreen() {
    val s = LocalAppStrings.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val user by SettingsStore.user.collectAsState()
    val language by SettingsStore.language.collectAsState()
    var userType by remember { mutableStateOf(UserType.HOUSEHOLD) }
    var showPicker by remember { mutableStateOf(false) }
    var showLangPicker by remember { mutableStateOf(false) }

    val destinations = HomeDestination.entries
    val pagerState = rememberPagerState(pageCount = { destinations.size })
    val destination = destinations[pagerState.currentPage]

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
                    ProfilePlaceholder(user = user)
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
                    Text(s.settings, modifier = Modifier.padding(8.dp))
                    Spacer(Modifier.weight(1f))
                    HorizontalDivider()
                    LanguageSwitcher(
                        currentLang = language,
                        onClick = { showLangPicker = true }
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
                    onSwitchClick = { showPicker = true }
                )
            }
        ) { innerPadding ->
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                userScrollEnabled = true
            ) { page ->
                when (destinations[page]) {
                    HomeDestination.DASHBOARD -> when (userType) {
                        UserType.HOUSEHOLD -> HouseholdDashboard()
                        UserType.COLLECTOR -> CollectorDashboard(user = user)
                        UserType.VOLUNTEER -> VolunteerDashboard(user = user)
                    }
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
