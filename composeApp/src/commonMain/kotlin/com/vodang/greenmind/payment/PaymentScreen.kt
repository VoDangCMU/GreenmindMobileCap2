package com.vodang.greenmind.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

import com.vodang.greenmind.api.payment.InvoiceDto
import com.vodang.greenmind.api.payment.PaymentRecordDto
import com.vodang.greenmind.components.AppScaffold
import com.vodang.greenmind.navigation.AppScreen
import com.vodang.greenmind.navigation.Navigation
import com.vodang.greenmind.store.PaymentStore
import com.vodang.greenmind.theme.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

private val blue700 = Color(0xFF1976D2)
private val blue50 = Color(0xFFE3F2FD)
private val blueIcon = Color(0xFF1565C0)
private val green50 = Color(0xFFE8F5E9)
private val greenIcon = Color(0xFF2E7D32)
private val orange50 = Color(0xFFFFF3E0)
private val orangeIcon = Color(0xFFE65100)
private val red50 = Color(0xFFFFEBEE)
private val redIcon = Color(0xFFC62828)
private val purple50 = Color(0xFFF3E5F5)
private val purpleIcon = Color(0xFF6A1B9A)
private val SurfaceWhite = Color(0xFFFFFFFF)
private val SurfaceGray = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFF757575)

enum class PaymentTab {
    PENDING, PAID, ALL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen() {
    val s = com.vodang.greenmind.i18n.LocalAppStrings.current
    var selectedTab by remember { mutableStateOf(PaymentTab.PENDING) }
    var selectedInvoice by remember { mutableStateOf<InvoiceDto?>(null) }
    var showPaymentSheet by remember { mutableStateOf(false) }
    var showPaymentSuccess by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // State from PaymentStore
    val paymentState by PaymentStore.state.collectAsState()
    val invoices = paymentState.invoices
    val isLoading = paymentState.isLoading

    // Filter invoices by tab
    val filteredInvoices = when (selectedTab) {
        PaymentTab.PENDING -> invoices.filter { it.status == "PENDING" || it.status == "OVERDUE" }
        PaymentTab.PAID -> invoices.filter { it.status == "PAID" }
        PaymentTab.ALL -> invoices
    }

    // Load invoices on first composition
    LaunchedEffect(Unit) {
        // TODO: Replace with actual access token from SettingsStore
        PaymentStore.fetchInvoices("mock_token", null)
    }

    AppScaffold(
        title = s.paymentTitle,
        showBackButton = true,
        onBackClick = { Navigation.goBack() },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(SurfaceGray)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Summary Cards ────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PaymentSummaryCard(
                    label = s.paymentPending,
                    value = invoices.filter { it.status == "PENDING" || it.status == "OVERDUE" }.sumOf { it.amount - it.amountPaid },
                    icon = Icons.Filled.Schedule,
                    backgroundColor = orange50,
                    iconColor = orangeIcon,
                    modifier = Modifier.weight(1f)
                )
                PaymentSummaryCard(
                    label = s.paymentPaid,
                    value = invoices.filter { it.status == "PAID" }.sumOf { it.amountPaid },
                    icon = Icons.Filled.CheckCircle,
                    backgroundColor = green50,
                    iconColor = greenIcon,
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Tab Selector ─────────────────────────────────────────────────────
            PaymentTabSelector(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it },
                pendingCount = invoices.count { it.status == "PENDING" || it.status == "OVERDUE" },
                paidCount = invoices.count { it.status == "PAID" },
                allCount = invoices.size
            )

            // ── Invoice List ────────────────────────────────────────────────────
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Green800)
                }
            } else if (filteredInvoices.isEmpty()) {
                PaymentEmptyState(
                    message = when (selectedTab) {
                        PaymentTab.PENDING -> s.paymentNoPending
                        PaymentTab.PAID -> s.paymentNoPaid
                        PaymentTab.ALL -> s.paymentNoInvoices
                    }
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredInvoices, key = { it.id }) { invoice ->
                        InvoiceCard(
                            invoice = invoice,
                            onClick = {
                                selectedInvoice = invoice
                                showPaymentSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    // ── Payment Detail Bottom Sheet ──────────────────────────────────────────
    if (showPaymentSheet && selectedInvoice != null) {
        ModalBottomSheet(
            onDismissRequest = {
                showPaymentSheet = false
                selectedInvoice = null
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            PaymentDetailSheet(
                invoice = selectedInvoice!!,
                onPayClick = {
                    // TODO: Implement Stripe payment
                    // For now, show success dialog
                    coroutineScope.launch {
                        showPaymentSheet = false
                        showPaymentSuccess = true
                        selectedInvoice = null
                    }
                },
                onCancel = {
                    showPaymentSheet = false
                    selectedInvoice = null
                }
            )
        }
    }

    // ── Payment Success Dialog ─────────────────────────────────────────────
    if (showPaymentSuccess) {
        AlertDialog(
            onDismissRequest = { showPaymentSuccess = false },
            confirmButton = {
                TextButton(onClick = { showPaymentSuccess = false }) {
                    Text(s.close, color = Green800)
                }
            },
            icon = {
                Box(
                    modifier = Modifier.size(56.dp).clip(CircleShape).background(green50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Green800, modifier = Modifier.size(32.dp))
                }
            },
            title = { Text(s.paymentSuccessTitle, fontWeight = FontWeight.Bold) },
            text = { Text(s.paymentSuccessDesc) }
        )
    }
}

@Composable
private fun PaymentSummaryCard(
    label: String,
    value: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
            }
            Column {
                Text(
                    text = formatCurrency(value),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                Text(text = label, fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun PaymentTabSelector(
    selectedTab: PaymentTab,
    onTabSelected: (PaymentTab) -> Unit,
    pendingCount: Int,
    paidCount: Int,
    allCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceWhite),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        PaymentTabButton(
            label = com.vodang.greenmind.i18n.LocalAppStrings.current.paymentPending,
            count = pendingCount,
            isSelected = selectedTab == PaymentTab.PENDING,
            onClick = { onTabSelected(PaymentTab.PENDING) },
            modifier = Modifier.weight(1f)
        )
        PaymentTabButton(
            label = com.vodang.greenmind.i18n.LocalAppStrings.current.paymentPaid,
            count = paidCount,
            isSelected = selectedTab == PaymentTab.PAID,
            onClick = { onTabSelected(PaymentTab.PAID) },
            modifier = Modifier.weight(1f)
        )
        PaymentTabButton(
            label = com.vodang.greenmind.i18n.LocalAppStrings.current.paymentAll,
            count = allCount,
            isSelected = selectedTab == PaymentTab.ALL,
            onClick = { onTabSelected(PaymentTab.ALL) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PaymentTabButton(
    label: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Green800.copy(alpha = 0.1f) else Color.Transparent
    val textColor = if (isSelected) Green800 else TextSecondary

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = textColor)
                Spacer(Modifier.height(2.dp))
                Text(
                    count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Green800 else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun InvoiceCard(
    invoice: InvoiceDto,
    onClick: () -> Unit
) {
    val s = com.vodang.greenmind.i18n.LocalAppStrings.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = invoice.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Gray900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                InvoiceStatusBadge(status = invoice.status)
            }

            // Amount Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatCurrency(invoice.amount),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )
                if (invoice.status == "PENDING" || invoice.status == "OVERDUE") {
                    Text(
                        text = formatDate(invoice.dueDate),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (invoice.status == "OVERDUE") ErrorRed else TextSecondary
                    )
                } else if (invoice.status == "PAID") {
                    Text(
                        text = formatDate(invoice.paidAt ?: invoice.updatedAt),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = SuccessGreen
                    )
                }
            }

            // Pay Button (for pending invoices)
            if (invoice.status == "PENDING" || invoice.status == "OVERDUE") {
                Button(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Green800),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(s.paymentPayNow, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun InvoiceStatusBadge(status: String) {
    val (backgroundColor, textColor, label) = when (status) {
        "PENDING" -> Triple(orange50, orangeIcon, com.vodang.greenmind.i18n.LocalAppStrings.current.paymentStatusPending)
        "PAID" -> Triple(green50, greenIcon, com.vodang.greenmind.i18n.LocalAppStrings.current.paymentStatusPaid)
        "OVERDUE" -> Triple(red50, redIcon, com.vodang.greenmind.i18n.LocalAppStrings.current.paymentStatusOverdue)
        "CANCELLED" -> Triple(Gray100, Gray600, com.vodang.greenmind.i18n.LocalAppStrings.current.paymentStatusCancelled)
        else -> Triple(Gray100, Gray600, status)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor
        )
    }
}

@Composable
private fun PaymentDetailSheet(
    invoice: InvoiceDto,
    onPayClick: () -> Unit,
    onCancel: () -> Unit
) {
    val s = com.vodang.greenmind.i18n.LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with amount
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp)).background(blue50),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Receipt, contentDescription = null, tint = blueIcon, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(invoice.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Gray900)
                Text(formatCurrency(invoice.amount), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Green800)
            }
        }

        // Due date for pending invoices
        if (invoice.status == "PENDING" || invoice.status == "OVERDUE") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = if (invoice.status == "OVERDUE") red50 else orange50)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(s.paymentDue, fontSize = 14.sp, color = if (invoice.status == "OVERDUE") redIcon else orangeIcon)
                    Text(formatDate(invoice.dueDate), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (invoice.status == "OVERDUE") redIcon else orangeIcon)
                }
            }
        }

        // Stripe Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = blue50.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.Lock, contentDescription = null, tint = blueIcon, modifier = Modifier.size(20.dp))
                Text(
                    text = s.paymentStripeSecure,
                    fontSize = 12.sp,
                    color = blueIcon,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Action Buttons - Equal width
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(s.cancel, color = TextSecondary)
            }
            Button(
                onClick = onPayClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Green800),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.CreditCard, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.paymentPayNow, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Gray800)
    }
}

@Composable
private fun PaymentEmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier.size(80.dp).clip(CircleShape).background(gray100),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.ReceiptLong, contentDescription = null, tint = Gray400, modifier = Modifier.size(40.dp))
        }
        Text(message, fontSize = 14.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return format.format(amount)
}

private fun formatDate(dateString: String?): String {
    if (dateString == null) return ""
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = inputFormat.parse(dateString)
        outputFormat.format(date!!)
    } catch (e: Exception) {
        dateString
    }
}

private val DividerColor = Color(0xFFE0E0E0)
private val Gray900 = Color(0xFF212121)
private val Gray800 = Color(0xFF424242)
private val Gray600 = Color(0xFF757575)
private val Gray400 = Color(0xFFBDBDBD)
private val Gray100 = Color(0xFFF5F5F5)
private val ErrorRed = Color(0xFFD32F2F)
private val SuccessGreen = Color(0xFF388E3C)
private val Green800 = Color(0xFF2E7D32)
private val gray100 = Color(0xFFF5F5F5)