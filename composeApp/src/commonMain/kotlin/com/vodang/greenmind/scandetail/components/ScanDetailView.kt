package com.vodang.greenmind.scandetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
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
import com.vodang.greenmind.api.households.bringOutDetectTrash
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.scandetail.DisplayMode
import com.vodang.greenmind.scandetail.ScanDetailData
import com.vodang.greenmind.scandetail.ScanImpact
import com.vodang.greenmind.scandetail.neutralGray400
import com.vodang.greenmind.scandetail.neutralGray700
import com.vodang.greenmind.scandetail.scanGreen
import com.vodang.greenmind.scandetail.scanGreenBg
import com.vodang.greenmind.scandetail.scanGray
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import com.vodang.greenmind.wastesort.WasteSortStatus
import kotlinx.coroutines.launch

private val stepDone    = Color(0xFF2E7D32)
private val stepPending = Color(0xFFBDBDBD)
private val stepBg      = Color(0xFFE8F5E9)

private val STEPS = listOf(
    WasteSortStatus.SCANNED,
    WasteSortStatus.SORTED,
    WasteSortStatus.BRINGOUTED,
    WasteSortStatus.COLLECTED,
)

val label: (WasteSortStatus) -> String = { status ->
    when (status) {
        WasteSortStatus.SCANNED    -> "Scanned"
        WasteSortStatus.SORTED     -> "Sorted"
        WasteSortStatus.BRINGOUTED -> "Brought Out"
        WasteSortStatus.COLLECTED  -> "Collected"
    }
}

// ── Lifecycle Progress Bar (moved from ScanDetailCard) ───────────────────────

@Composable
fun LifecycleProgressBar(currentStatus: WasteSortStatus, modifier: Modifier = Modifier) {
    val currentIdx = STEPS.indexOf(currentStatus)
    val targetFraction = currentIdx.toFloat() / (STEPS.size - 1).toFloat()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(26.dp)) {
            val colWidth   = maxWidth / STEPS.size
            val trackStart = colWidth / 2
            val trackLen   = maxWidth - colWidth

            Box(
                modifier = Modifier
                    .offset(x = trackStart, y = 10.dp)
                    .width(trackLen)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(stepPending.copy(alpha = 0.25f)),
            )
            Box(
                modifier = Modifier
                    .offset(x = trackStart, y = 10.dp)
                    .width(trackLen * targetFraction)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(stepDone),
            )
            Row(modifier = Modifier.fillMaxWidth().height(26.dp)) {
                STEPS.forEachIndexed { idx, _ ->
                    val done   = idx <= currentIdx
                    val active = idx == currentIdx
                    Box(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (active) {
                            Box(
                                modifier = Modifier
                                    .size(22.dp)
                                    .clip(CircleShape)
                                    .background(stepDone.copy(alpha = 0.15f)),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(if (active) 14.dp else 10.dp)
                                .clip(CircleShape)
                                .background(if (done) stepDone else stepPending.copy(alpha = 0.45f)),
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            STEPS.forEachIndexed { idx, step ->
                val done   = idx <= currentIdx
                val active = idx == currentIdx
                Text(
                    text = label(step),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp),
                    fontSize = 10.sp,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    color = if (done) stepDone else stepPending,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

// ── Fixed Bottom Bar ─────────────────────────────────────────────────────────

@Composable
fun FixedBottomBar(
    status: WasteSortStatus,
    backendId: String?,
    onStatusChange: (WasteSortStatus) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    var isBusy by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        // Status badge row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Status", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = neutralGray700)
            StatusBadge(status = status)
        }

        HorizontalDivider(color = Color(0xFFE0E0E0))

        // Action button
        when (status) {
            WasteSortStatus.SCANNED -> {
                Button(
                    onClick = { onStatusChange(WasteSortStatus.SORTED) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = stepDone),
                ) {
                    Text(s.markAsSorted, fontWeight = FontWeight.Bold)
                }
            }

            WasteSortStatus.SORTED -> {
                Button(
                    onClick = {
                        if (backendId == null) {
                            onStatusChange(WasteSortStatus.BRINGOUTED)
                            return@Button
                        }
                        isBusy = true
                        errorMsg = null
                        scope.launch {
                            try {
                                val token = SettingsStore.getAccessToken()
                                    ?: throw IllegalStateException("Not signed in")
                                bringOutDetectTrash(token, backendId)
                                onStatusChange(WasteSortStatus.BRINGOUTED)
                            } catch (e: Throwable) {
                                AppLogger.e("ScanDetailView", "bring-out failed: ${e.message}")
                                errorMsg = e.message ?: "Failed to update status"
                            } finally {
                                isBusy = false
                            }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(8.dp))
                        Text("Updating…", fontWeight = FontWeight.Bold)
                    } else {
                        Text(s.markAsBroughtOut, fontWeight = FontWeight.Bold)
                    }
                }
                errorMsg?.let {
                    Text(it, color = Color(0xFFD32F2F), fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                }
            }

            WasteSortStatus.BRINGOUTED -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(s.waitingForCollector, fontSize = 14.sp, color = Color(0xFF757575), fontWeight = FontWeight.Medium)
                }
            }

            WasteSortStatus.COLLECTED -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(s.collectedComplete, fontSize = 14.sp, color = stepDone, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ── Status Badge ──────────────────────────────────────────────────────────────

@Composable
fun StatusBadge(status: WasteSortStatus, modifier: Modifier = Modifier) {
    val (bgColor, textColor, label) = when (status) {
        WasteSortStatus.SCANNED -> Triple(Color(0xFFFFF3E0), Color(0xFFE65100), "Scanned")
        WasteSortStatus.SORTED -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "Sorted")
        WasteSortStatus.BRINGOUTED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Brought Out")
        WasteSortStatus.COLLECTED -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "Collected")
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
        )
    }
}

// ── Main ScanDetailView ────────────────────────────────────────────────────────

@Composable
fun ScanDetailView(
    data: ScanDetailData,
    onBack: () -> Unit,
    onStatusChange: (WasteSortStatus) -> Unit = {},
    displayMode: DisplayMode = DisplayMode.FULL_SCREEN,
    modifier: Modifier = Modifier,
) {
    val s = LocalAppStrings.current

    val contentModifier = if (displayMode == DisplayMode.BOTTOM_SHEET) {
        Modifier.fillMaxWidth()
    } else {
        Modifier.fillMaxSize()
    }

    Box(modifier = contentModifier.background(Color(0xFFF5F5F5))) {
        Column(modifier = if (displayMode == DisplayMode.BOTTOM_SHEET) Modifier.fillMaxWidth() else Modifier.fillMaxSize()) {
            if (displayMode == DisplayMode.BOTTOM_SHEET) {
                // Bottom sheet: use verticalScroll instead of LazyColumn
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                ) {
                    EcoScoreSection(greenScore = data.greenScore)
                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    ImagesSection(
                        imageUrl = data.imageUrl,
                        annotatedImageUrl = data.annotatedImageUrl,
                        aiAnalysisUrl = data.aiAnalysisUrl,
                        depthMapUrl = data.depthMapUrl,
                    )
                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    ImpactMeterSection(impact = data.impact)
                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    PollutantBreakdownSection(pollution = data.pollution)
                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    ItemsSection(items = data.items)
                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    MassSection(totalMassKg = data.totalMassKg, itemsMass = data.itemsMass)
                    HorizontalDivider(color = Color(0xFFE0E0E0))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 20.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Scanned by", fontSize = 12.sp, color = neutralGray400)
                        Text(data.scannedBy, fontSize = 12.sp, color = neutralGray700, fontWeight = FontWeight.Medium)
                    }
                    HorizontalDivider(color = Color(0xFFE0E0E0))
                }

                FixedBottomBar(
                    status = data.status,
                    backendId = data.backendId,
                    onStatusChange = onStatusChange,
                )
            } else {
                // Full screen: use LazyColumn
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(0.dp),
                    contentPadding = PaddingValues(bottom = 8.dp),
                ) {
                    item {
                        EcoScoreSection(greenScore = data.greenScore)
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }

                    item {
                        ImagesSection(
                            imageUrl = data.imageUrl,
                            annotatedImageUrl = data.annotatedImageUrl,
                            aiAnalysisUrl = data.aiAnalysisUrl,
                            depthMapUrl = data.depthMapUrl,
                        )
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }

                    item {
                        ImpactMeterSection(impact = data.impact)
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }

                    item {
                        PollutantBreakdownSection(pollution = data.pollution)
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }

                    item {
                        ItemsSection(items = data.items)
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }

                    item {
                        MassSection(totalMassKg = data.totalMassKg, itemsMass = data.itemsMass)
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 20.dp, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Scanned by", fontSize = 12.sp, color = neutralGray400)
                            Text(data.scannedBy, fontSize = 12.sp, color = neutralGray700, fontWeight = FontWeight.Medium)
                        }
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }
                }

                FixedBottomBar(
                    status = data.status,
                    backendId = data.backendId,
                    onStatusChange = onStatusChange,
                )
            }
        }
    }
}