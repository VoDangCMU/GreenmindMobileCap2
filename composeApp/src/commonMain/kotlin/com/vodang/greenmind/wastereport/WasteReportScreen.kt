package com.vodang.greenmind.wastereport

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.wastereport.CreateWasteReportRequest
import com.vodang.greenmind.api.wastereport.WasteReportDto
import com.vodang.greenmind.api.wastereport.createWasteReport
import com.vodang.greenmind.api.wastereport.getAllWasteReports
import com.vodang.greenmind.api.wastereport.getMyWasteReports
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import kotlinx.coroutines.launch

private val green800 = Color(0xFF2E7D32)

@Composable
fun WasteReportScreen() {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    var showScan by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedReport by remember { mutableStateOf<WasteReportDto?>(null) }

    var myReports by remember { mutableStateOf<List<WasteReportDto>>(emptyList()) }
    var allReports by remember { mutableStateOf<List<WasteReportDto>>(emptyList()) }
    var isLoadingMy by remember { mutableStateOf(true) }
    var isLoadingAll by remember { mutableStateOf(false) }
    var allLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken() ?: run { isLoadingMy = false; return@LaunchedEffect }
        try {
            myReports = getMyWasteReports(token).data
        } catch (e: Throwable) {
            AppLogger.e("WasteReport", "Load my reports failed: ${e.message}")
        }
        isLoadingMy = false
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 1 && !allLoaded) {
            isLoadingAll = true
            val token = SettingsStore.getAccessToken() ?: run { isLoadingAll = false; return@LaunchedEffect }
            try {
                allReports = getAllWasteReports(token).data
                allLoaded = true
            } catch (e: Throwable) {
                AppLogger.e("WasteReport", "Load all reports failed: ${e.message}")
            }
            isLoadingAll = false
        }
    }

    if (showScan) {
        WasteReportScanScreen(
            onReported = { form ->
                scope.launch {
                    val token = SettingsStore.getAccessToken() ?: return@launch
                    try {
                        val created = createWasteReport(
                            token,
                            CreateWasteReportRequest(
                                wasteType   = form.wasteType,
                                wardName    = form.wardName,
                                lat         = form.lat,
                                lng         = form.lng,
                                description = form.description,
                                imageKey    = form.imageKey,
                                imageUrl    = form.imageUrl,
                            )
                        )
                        myReports = listOf(created) + myReports
                        if (allLoaded) allReports = listOf(created) + allReports
                    } catch (e: Throwable) {
                        AppLogger.e("WasteReport", "Create failed: ${e.message}")
                    }
                }
                showScan = false
            },
            onBack = { showScan = false }
        )
        return
    }

    val myListState  = rememberLazyListState()
    val allListState = rememberLazyListState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Column(modifier = Modifier.fillMaxSize()) {
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = green800,
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            s.wasteReportMyTab,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            s.wasteReportAllTab,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp,
                        )
                    }
                )
            }

            val items = if (selectedTab == 0) myReports else allReports
            val isLoading = if (selectedTab == 0) isLoadingMy else isLoadingAll
            val listState = if (selectedTab == 0) myListState else allListState

            when {
                isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = green800)
                }

                items.isEmpty() -> Box(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🗑️", fontSize = 48.sp)
                        Text(
                            s.wasteReportEmpty,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                        )
                    }
                }

                else -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(items, key = { it.id }) { report ->
                        WasteReportCard(report, onClick = { selectedReport = report })
                    }
                    item { Spacer(Modifier.height(72.dp)) } // FAB clearance
                }
            }
        }

        FloatingActionButton(
            onClick = { showScan = true },
            containerColor = green800,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding(),
        ) {
            Text("+", fontSize = 28.sp, fontWeight = FontWeight.Light)
        }
    }

    selectedReport?.let { report ->
        WasteReportDetailSheet(report = report, onDismiss = { selectedReport = null })
    }
}

