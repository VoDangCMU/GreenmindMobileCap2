package com.vodang.greenmind.bill

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vodang.greenmind.api.invoicepollution.toWasteDetectResponse
import com.vodang.greenmind.api.ocr.InvoiceDto
import com.vodang.greenmind.api.ocr.getInvoices
import com.vodang.greenmind.home.components.EnvImpactCard
import com.vodang.greenmind.i18n.LocalAppStrings
import com.vodang.greenmind.store.BillRecord
import com.vodang.greenmind.store.BillStore
import com.vodang.greenmind.store.SettingsStore
import com.vodang.greenmind.util.AppLogger
import com.vodang.greenmind.wastereport.NetworkImage

private val green800 = Color(0xFF2E7D32)
private val green600 = Color(0xFF388E3C)
private val green50  = Color(0xFFE8F5E9)
private val orange   = Color(0xFFE65100)
private val red      = Color(0xFFC62828)
private val gray50   = Color(0xFFF5F5F5)

private fun ratioColor(ratio: Int): Color = when {
    ratio >= 70 -> green800
    ratio >= 40 -> orange
    else        -> red
}

private fun currencySymbol(currency: String?): String = when (currency?.uppercase()) {
    "USD" -> "$"
    "EUR" -> "€"
    "GBP" -> "£"
    "VND" -> "₫"
    else  -> currency?.let { "$it " } ?: "$"
}

private fun formatAmount(amount: Double, symbol: String = "$"): String =
    "$symbol${"%.2f".format(amount)}"

@Composable
fun BillListScreen(onScanClick: () -> Unit) {
    val s = LocalAppStrings.current

    val localBills by BillStore.bills.collectAsState()
    var invoices by remember { mutableStateOf<List<InvoiceDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var selectedInvoice by remember { mutableStateOf<InvoiceDto?>(null) }
    var selectedLocalBill by remember { mutableStateOf<BillRecord?>(null) }

    LaunchedEffect(Unit) {
        val token = SettingsStore.getAccessToken()
        if (token == null) { isLoading = false; return@LaunchedEffect }
        try {
            invoices = getInvoices(token)
        } catch (e: Throwable) {
            AppLogger.e("BillList", "Failed to load invoices: ${e.message}")
            errorMsg = "Could not load history. Pull down to retry."
        }
        isLoading = false
    }

    Box(modifier = Modifier.fillMaxSize().background(gray50)) {
        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = green800)
            }

            invoices.isEmpty() && localBills.isEmpty() && errorMsg == null -> Box(
                Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text("🧾", fontSize = 48.sp)
                    Text(s.billListEmpty, color = Color.Gray, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(green800)
                            .clickable { onScanClick() }
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("📷", fontSize = 15.sp)
                        Text(s.billScanTitle, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                if (localBills.isNotEmpty()) {
                    item {
                        Text("Recent Scans", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                    }
                    items(localBills, key = { "local_${it.id}" }) { bill ->
                        LocalBillCard(bill, onClick = { selectedLocalBill = bill })
                    }
                    if (invoices.isNotEmpty()) {
                        item {
                            Text("History", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        }
                    }
                }
                if (errorMsg != null) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFEBEE))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Text(errorMsg!!, fontSize = 12.sp, color = red, modifier = Modifier.weight(1f))
                        }
                    }
                }
                items(invoices, key = { it.id }) { invoice ->
                    InvoiceCard(invoice, onClick = { selectedInvoice = invoice })
                }
            }
        }

        // ── Scan FAB ──────────────────────────────────────────────────────────
        FloatingActionButton(
            onClick = onScanClick,
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

    // ── Detail bottom sheets ──────────────────────────────────────────────────
    selectedInvoice?.let { invoice ->
        InvoiceDetailSheet(invoice = invoice, onDismiss = { selectedInvoice = null })
    }
    selectedLocalBill?.let { bill ->
        LocalBillDetailSheet(bill = bill, onDismiss = { selectedLocalBill = null })
    }
}

// ── Invoice row card ──────────────────────────────────────────────────────────

@Composable
private fun InvoiceCard(invoice: InvoiceDto, onClick: () -> Unit) {
    val ratio  = invoice.greenRatio()
    val color  = ratioColor(ratio)
    val symbol = currencySymbol(invoice.doc?.currency)
    val grand  = invoice.totals?.grandTotalDouble() ?: 0.0

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Green ratio badge
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("$ratio%", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // Middle — vendor + date
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    invoice.vendor?.name ?: "Unknown vendor",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B5E20),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val dateLine = listOfNotNull(invoice.datetime?.date, invoice.datetime?.time)
                    .joinToString("  ·  ")
                if (dateLine.isNotBlank()) {
                    Text(dateLine, fontSize = 12.sp, color = Color.Gray)
                }
                val itemCount = invoice.items?.size ?: 0
                Text("$itemCount item${if (itemCount != 1) "s" else ""}", fontSize = 11.sp, color = Color(0xFF9E9E9E))
            }

            // Right — grand total
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(formatAmount(grand, symbol), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
                val plant = invoice.items?.filter { it.plantBased == true }?.sumOf { it.lineTotal ?: 0.0 } ?: 0.0
                Text("🌿 ${formatAmount(plant, symbol)}", fontSize = 11.sp, color = green600)
            }
        }
    }
}

// ── Detail bottom sheet ───────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvoiceDetailSheet(invoice: InvoiceDto, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val symbol     = currencySymbol(invoice.doc?.currency)
    val totals     = invoice.totals

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Text(
                invoice.vendor?.name ?: "Invoice",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B1B1B),
            )

            // Meta
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!invoice.vendor?.address.isNullOrBlank())
                    SheetRow("📍", invoice.vendor.address)
                val dateLine = listOfNotNull(invoice.datetime?.date, invoice.datetime?.time).joinToString("  ·  ")
                if (dateLine.isNotBlank()) SheetRow("🗓", dateLine)
                if (!invoice.doc?.paymentMethod.isNullOrBlank())
                    SheetRow("💳", invoice.doc.paymentMethod)
                if (!invoice.doc?.notes.isNullOrBlank())
                    SheetRow("📝", invoice.doc.notes)
            }

            // Items
            if (!invoice.items.isNullOrEmpty()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text("Items", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Header row
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Item", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                        Text("Qty", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(28.dp))
                        Text("Unit", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(54.dp))
                        Text("Total", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(56.dp))
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    invoice.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(if (item.plantBased == true) "🌿" else "·", fontSize = 12.sp)
                                    Text(
                                        item.rawName ?: "Unknown",
                                        fontSize = 13.sp,
                                        color = Color(0xFF212121),
                                        fontWeight = FontWeight.Medium,
                                    )
                                }
                                if (!item.brand.isNullOrBlank()) {
                                    Text(
                                        item.brand,
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 18.dp),
                                    )
                                }
                            }
                            Text("${item.quantity ?: "—"}", fontSize = 12.sp, color = Color(0xFF616161), modifier = Modifier.width(28.dp))
                            Text(item.unitPrice?.let { formatAmount(it, symbol) } ?: "—", fontSize = 12.sp, color = Color(0xFF616161), modifier = Modifier.width(54.dp))
                            Text(
                                item.lineTotal?.let { formatAmount(it, symbol) } ?: "—",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (item.plantBased == true) green600 else Color(0xFF424242),
                                modifier = Modifier.width(56.dp),
                            )
                        }
                    }
                }
            }

            // Totals
            if (totals != null) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text("Totals", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    totals.subtotalDouble().let  { TotalsRow("Subtotal", formatAmount(it, symbol)) }
                    totals.discountDouble().let  { if (it != 0.0) TotalsRow("Discount", "− ${formatAmount(it, symbol)}", red) }
                    totals.taxDouble().let       { if (it != 0.0) TotalsRow("Tax", formatAmount(it, symbol)) }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    TotalsRow("Grand Total", formatAmount(totals.grandTotalDouble(), symbol), green800, bold = true)
                }
            }
        }
    }
}

@Composable
private fun SheetRow(icon: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
        Text(icon, fontSize = 13.sp)
        Text(text, fontSize = 13.sp, color = Color(0xFF616161), lineHeight = 18.sp, modifier = Modifier.weight(1f))
    }
}

// ── Local scan card ───────────────────────────────────────────────────────────

@Composable
private fun LocalBillCard(bill: BillRecord, onClick: () -> Unit) {
    val color  = ratioColor(bill.greenRatio)
    val date   = formatMillis(bill.timestampMillis)

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            if (!bill.imageUrl.isNullOrBlank()) {
                NetworkImage(
                    url = bill.imageUrl,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(10.dp)),
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("${bill.greenRatio}%", color = color, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    bill.storeName.ifBlank { "Unknown vendor" },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1B5E20),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(date, fontSize = 12.sp, color = Color.Gray)
                Text("🌿 ${bill.greenRatio}% green", fontSize = 11.sp, color = green600)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(formatAmount(bill.totalAmount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF424242))
                Text(formatAmount(bill.greenAmount), fontSize = 11.sp, color = green600)
            }
        }
    }
}

// ── Local bill detail sheet ───────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocalBillDetailSheet(bill: BillRecord, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val color      = ratioColor(bill.greenRatio)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    bill.storeName.ifBlank { "Unknown vendor" },
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B1B1B),
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(color.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text("${bill.greenRatio}% green", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
                }
            }

            SheetRow("🗓", formatMillis(bill.timestampMillis))

            // Scan image
            if (!bill.imageUrl.isNullOrBlank()) {
                NetworkImage(
                    url = bill.imageUrl,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                )
            }

            // Environmental impact
            bill.pollutionResult?.let { pollution ->
                EnvImpactCard(result = pollution.toWasteDetectResponse())
            }

            // Items
            if (bill.items.isNotEmpty()) {
                HorizontalDivider(color = Color(0xFFEEEEEE))
                Text("Items", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Text("Item", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.weight(1f))
                        Text("Total", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.width(72.dp))
                    }
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    bill.items.forEach { item ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(if (item.isGreen) "🌿" else "·", fontSize = 12.sp)
                                Text(
                                    item.name,
                                    fontSize = 13.sp,
                                    color = Color(0xFF212121),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                )
                            }
                            Text(
                                formatAmount(item.amount),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (item.isGreen) green600 else Color(0xFF424242),
                                modifier = Modifier.width(72.dp),
                            )
                        }
                    }
                }
            }

            // Totals
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Text("Totals", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF616161))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                TotalsRow("Green items", formatAmount(bill.greenAmount), green600)
                HorizontalDivider(color = Color(0xFFF0F0F0))
                TotalsRow("Grand Total", formatAmount(bill.totalAmount), green800, bold = true)
            }
        }
    }
}

private fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = (totalSeconds / 60) % 60
    val hours   = (totalSeconds / 3600) % 24
    // Approximate date from epoch — good enough for display
    val days    = (totalSeconds / 86400).toInt()
    val year    = 1970 + days / 365
    val dayOfYear = days % 365
    val month   = dayOfYear / 30 + 1
    val day     = dayOfYear % 30 + 1
    return "%02d/%02d/%d  %02d:%02d".format(day, month, year, hours, minutes)
}

@Composable
private fun TotalsRow(label: String, value: String, valueColor: Color = Color(0xFF424242), bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, fontSize = 13.sp, color = Color(0xFF757575))
        Text(
            value,
            fontSize = if (bold) 15.sp else 13.sp,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = valueColor,
        )
    }
}
