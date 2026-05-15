package com.vodang.greenmind.householdwaste.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.theme.Green800
import com.vodang.greenmind.theme.TextSecondary
import com.vodang.greenmind.wastesort.WasteSortStatus

enum class DateFilter { TODAY, LAST_7_DAYS, LAST_30_DAYS, ALL_TIME }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WasteStatusFilterSheet(
    selectedStatus: WasteSortStatus?,
    selectedDateFilter: DateFilter,
    onStatusSelected: (WasteSortStatus?) -> Unit,
    onDateFilterSelected: (DateFilter) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
) {
    val s = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = androidx.compose.ui.graphics.Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text(
                "Filter & Sort",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color(0xFF374151),
            )

            // Status filter section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Filter by status",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedStatus == null,
                        onClick = { onStatusSelected(null) },
                        label = { Text("All") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Green800,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    )
                    FilterChip(
                        selected = selectedStatus == WasteSortStatus.SORTED,
                        onClick = { onStatusSelected(WasteSortStatus.SORTED) },
                        label = { Text("Sorted") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = androidx.compose.ui.graphics.Color(0xFF1565C0),
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    )
                    FilterChip(
                        selected = selectedStatus == WasteSortStatus.BRINGOUTED,
                        onClick = { onStatusSelected(WasteSortStatus.BRINGOUTED) },
                        label = { Text("Brought Out") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = androidx.compose.ui.graphics.Color(0xFFB45309),
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    )
                    FilterChip(
                        selected = selectedStatus == WasteSortStatus.COLLECTED,
                        onClick = { onStatusSelected(WasteSortStatus.COLLECTED) },
                        label = { Text("Collected") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    )
                }
            }

            // Date filter section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Filter by date",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedDateFilter == DateFilter.TODAY,
                        onClick = { onDateFilterSelected(DateFilter.TODAY) },
                        label = { Text("Today") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Green800,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    )
                    FilterChip(
                        selected = selectedDateFilter == DateFilter.LAST_7_DAYS,
                        onClick = { onDateFilterSelected(DateFilter.LAST_7_DAYS) },
                        label = { Text("7 days") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Green800,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = selectedDateFilter == DateFilter.LAST_30_DAYS,
                        onClick = { onDateFilterSelected(DateFilter.LAST_30_DAYS) },
                        label = { Text("30 days") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Green800,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    )
                    FilterChip(
                        selected = selectedDateFilter == DateFilter.ALL_TIME,
                        onClick = { onDateFilterSelected(DateFilter.ALL_TIME) },
                        label = { Text("All Time") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Green800,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                        ),
                    )
                }
            }

            // Apply button
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green800),
            ) {
                Text("Apply", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}