package com.vodang.greenmind.scandetail.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vodang.greenmind.scandetail.DisplayMode
import com.vodang.greenmind.scandetail.ScanDetailData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetScanDetail(
    data: ScanDetailData,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        ScanDetailView(
            data = data,
            onBack = onDismiss,
            displayMode = DisplayMode.BOTTOM_SHEET,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 200.dp),
        )
    }
}