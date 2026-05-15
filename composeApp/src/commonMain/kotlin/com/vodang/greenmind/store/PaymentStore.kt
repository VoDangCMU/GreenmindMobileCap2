package com.vodang.greenmind.store

import com.vodang.greenmind.api.payment.InvoiceDto
import com.vodang.greenmind.api.payment.InvoiceListResponse
import com.vodang.greenmind.api.payment.PaymentIntentResponse
import com.vodang.greenmind.api.payment.PaymentRecordDto
import com.vodang.greenmind.api.payment.getInvoices
import com.vodang.greenmind.api.payment.getInvoiceById
import com.vodang.greenmind.api.payment.createPaymentIntent
import com.vodang.greenmind.api.payment.confirmPayment
import com.vodang.greenmind.api.payment.getPaymentHistory
import com.vodang.greenmind.api.payment.CreatePaymentIntentRequest
import com.vodang.greenmind.api.payment.ConfirmPaymentRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * PaymentStore - manages payment and invoice state.
 *
 * TODO: Backend integration required for all operations.
 * Currently returns mock data for UI development.
 */
object PaymentStore {

    // ── State ─────────────────────────────────────────────────────────────────

    data class PaymentState(
        val invoices: List<InvoiceDto> = emptyList(),
        val selectedInvoice: InvoiceDto? = null,
        val paymentHistory: List<PaymentRecordDto> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val currentPaymentIntent: PaymentIntentResponse? = null,
    )

    private val _state = MutableStateFlow(PaymentState())
    val state: StateFlow<PaymentState> = _state.asStateFlow()

    // ── Mock Data for UI Development ──────────────────────────────────────────

    /**
     * Returns mock invoices for UI development.
     * TODO: Replace with actual API call once backend is ready.
     */
    private fun getMockInvoices(): List<InvoiceDto> = listOf(
        InvoiceDto(
            id = "inv_001",
            code = "INV-2024-001",
            title = "Waste Collection Service",
            description = "Monthly waste collection fee",
            amount = 150000.0,
            amountPaid = 0.0,
            currency = "VND",
            status = "PENDING",
            dueDate = "2024-02-15",
            paidAt = null,
            createdAt = "2024-01-15",
            updatedAt = "2024-01-15",
            items = listOf(
                com.vodang.greenmind.api.payment.InvoiceItemDto(
                    id = "item_001",
                    name = "Waste Collection - January",
                    description = "Monthly household waste collection",
                    quantity = 1,
                    unitPrice = 100000.0,
                    lineTotal = 100000.0,
                ),
                com.vodang.greenmind.api.payment.InvoiceItemDto(
                    id = "item_002",
                    name = "Recycling Processing Fee",
                    description = "Recyclable materials processing",
                    quantity = 1,
                    unitPrice = 50000.0,
                    lineTotal = 50000.0,
                ),
            ),
            stripePaymentIntentId = null,
            stripeClientSecret = null,
        ),
        InvoiceDto(
            id = "inv_002",
            code = "INV-2024-002",
            title = "Environmental Impact Fee",
            description = "Quarterly environmental monitoring fee",
            amount = 75000.0,
            amountPaid = 75000.0,
            currency = "VND",
            status = "PAID",
            dueDate = "2024-01-30",
            paidAt = "2024-01-28",
            createdAt = "2024-01-01",
            updatedAt = "2024-01-28",
            items = listOf(
                com.vodang.greenmind.api.payment.InvoiceItemDto(
                    id = "item_003",
                    name = "Environmental Monitoring",
                    description = "Quarterly environmental impact assessment",
                    quantity = 1,
                    unitPrice = 75000.0,
                    lineTotal = 75000.0,
                ),
            ),
            stripePaymentIntentId = "pi_mock_001",
            stripeClientSecret = null,
        ),
        InvoiceDto(
            id = "inv_003",
            code = "INV-2023-015",
            title = "Household Waste Service",
            description = "December waste collection",
            amount = 150000.0,
            amountPaid = 0.0,
            currency = "VND",
            status = "OVERDUE",
            dueDate = "2024-01-01",
            paidAt = null,
            createdAt = "2023-12-01",
            updatedAt = "2023-12-01",
            items = listOf(
                com.vodang.greenmind.api.payment.InvoiceItemDto(
                    id = "item_004",
                    name = "Waste Collection - December",
                    description = "Monthly household waste collection",
                    quantity = 1,
                    unitPrice = 150000.0,
                    lineTotal = 150000.0,
                ),
            ),
            stripePaymentIntentId = null,
            stripeClientSecret = null,
        ),
    )

    private fun getMockPaymentHistory(): List<PaymentRecordDto> = listOf(
        PaymentRecordDto(
            id = "pay_001",
            invoiceId = "inv_002",
            invoiceCode = "INV-2024-002",
            amount = 75000.0,
            currency = "VND",
            status = "SUCCEEDED",
            paymentMethod = "card",
            paidAt = "2024-01-28T10:30:00Z",
            createdAt = "2024-01-28",
        ),
        PaymentRecordDto(
            id = "pay_002",
            invoiceId = "inv_00a",
            invoiceCode = "INV-2023-012",
            amount = 150000.0,
            currency = "VND",
            status = "SUCCEEDED",
            paymentMethod = "card",
            paidAt = "2023-12-15T14:22:00Z",
            createdAt = "2023-12-15",
        ),
    )

    // ── Actions ───────────────────────────────────────────────────────────────

    /**
     * Fetches all invoices for the current user.
     * TODO: Replace mock data with actual API call getInvoices(accessToken, status, page, limit)
     */
    suspend fun fetchInvoices(accessToken: String, status: String? = null) {
        AppLogger.i("PaymentStore", "fetchInvoices status=$status")
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            // TODO: Replace with actual API
            // val response = getInvoices(accessToken, status)
            // _state.update { it.copy(invoices = response.data, isLoading = false) }

            // Mock data for UI development
            val mockInvoices = getMockInvoices().let { invoices ->
                if (status != null) invoices.filter { it.status == status } else invoices
            }
            _state.update { it.copy(invoices = mockInvoices, isLoading = false) }
        } catch (e: Throwable) {
            AppLogger.e("PaymentStore", "fetchInvoices error: ${e.message}")
            _state.update { it.copy(error = e.message, isLoading = false) }
        }
    }

    /**
     * Fetches a single invoice by ID.
     * TODO: Replace mock data with actual API call getInvoiceById(accessToken, invoiceId)
     */
    suspend fun fetchInvoiceById(accessToken: String, invoiceId: String) {
        AppLogger.i("PaymentStore", "fetchInvoiceById invoiceId=$invoiceId")
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            // TODO: Replace with actual API
            // val invoice = getInvoiceById(accessToken, invoiceId)
            // _state.update { it.copy(selectedInvoice = invoice, isLoading = false) }

            // Mock data for UI development
            val invoice = getMockInvoices().find { it.id == invoiceId }
            _state.update { it.copy(selectedInvoice = invoice, isLoading = false) }
        } catch (e: Throwable) {
            AppLogger.e("PaymentStore", "fetchInvoiceById error: ${e.message}")
            _state.update { it.copy(error = e.message, isLoading = false) }
        }
    }

    /**
     * Creates a Stripe PaymentIntent for the given invoice.
     * TODO: Implement actual API call createPaymentIntent(accessToken, CreatePaymentIntentRequest(invoiceId))
     *
     * @return PaymentIntentResponse with clientSecret for Stripe SDK confirmation
     */
    suspend fun initiatePayment(accessToken: String, invoiceId: String): Result<PaymentIntentResponse> {
        AppLogger.i("PaymentStore", "initiatePayment invoiceId=$invoiceId")
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            // TODO: Replace with actual API
            // val response = createPaymentIntent(accessToken, CreatePaymentIntentRequest(invoiceId))
            // _state.update { it.copy(currentPaymentIntent = response, isLoading = false) }
            // return Result.success(response)

            // Mock response for UI development
            val mockResponse = PaymentIntentResponse(
                id = "pi_mock_${invoiceId}",
                clientSecret = "pi_mock_secret_${invoiceId}",
                publishableKey = "pk_test_mock_key",
                amount = 150000.0,
                currency = "vnd",
                status = "requires_payment_method",
            )
            _state.update { it.copy(currentPaymentIntent = mockResponse, isLoading = false) }
            return Result.success(mockResponse)
        } catch (e: Throwable) {
            AppLogger.e("PaymentStore", "initiatePayment error: ${e.message}")
            _state.update { it.copy(error = e.message, isLoading = false) }
            return Result.failure(e)
        }
    }

    /**
     * Confirms payment completion.
     * TODO: Implement actual API call confirmPayment(accessToken, ConfirmPaymentRequest(paymentIntentId))
     */
    suspend fun confirmPaymentCompletion(
        accessToken: String,
        paymentIntentId: String,
        paymentMethodId: String? = null,
    ): Result<PaymentRecordDto> {
        AppLogger.i("PaymentStore", "confirmPaymentCompletion paymentIntentId=$paymentIntentId")
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            // TODO: Replace with actual API
            // val response = confirmPayment(accessToken, ConfirmPaymentRequest(paymentIntentId, paymentMethodId))
            // _state.update { it.copy(isLoading = false) }
            // return Result.success(response)

            // Mock response for UI development
            val mockRecord = PaymentRecordDto(
                id = "pay_${System.currentTimeMillis()}",
                invoiceId = _state.value.selectedInvoice?.id ?: "",
                invoiceCode = _state.value.selectedInvoice?.code ?: "",
                amount = _state.value.selectedInvoice?.amount ?: 0.0,
                currency = "VND",
                status = "SUCCEEDED",
                paymentMethod = paymentMethodId ?: "card",
                paidAt = java.time.Instant.now().toString(),
                createdAt = java.time.Instant.now().toString(),
            )
            _state.update { it.copy(isLoading = false) }
            return Result.success(mockRecord)
        } catch (e: Throwable) {
            AppLogger.e("PaymentStore", "confirmPaymentCompletion error: ${e.message}")
            _state.update { it.copy(error = e.message, isLoading = false) }
            return Result.failure(e)
        }
    }

    /**
     * Fetches payment history.
     * TODO: Replace mock data with actual API call getPaymentHistory(accessToken, page, limit)
     */
    suspend fun fetchPaymentHistory(accessToken: String) {
        AppLogger.i("PaymentStore", "fetchPaymentHistory")
        _state.update { it.copy(isLoading = true, error = null) }
        try {
            // TODO: Replace with actual API
            // val response = getPaymentHistory(accessToken)
            // _state.update { it.copy(paymentHistory = response.data, isLoading = false) }

            // Mock data for UI development
            _state.update { it.copy(paymentHistory = getMockPaymentHistory(), isLoading = false) }
        } catch (e: Throwable) {
            AppLogger.e("PaymentStore", "fetchPaymentHistory error: ${e.message}")
            _state.update { it.copy(error = e.message, isLoading = false) }
        }
    }

    /**
     * Clears the selected invoice.
     */
    fun clearSelectedInvoice() {
        _state.update { it.copy(selectedInvoice = null) }
    }

    /**
     * Clears the current payment intent.
     */
    fun clearPaymentIntent() {
        _state.update { it.copy(currentPaymentIntent = null) }
    }

    /**
     * Clears any error state.
     */
    fun clearError() {
        _state.update { it.copy(error = null) }
    }
}

// Placeholder for AppLogger import
private object AppLogger {
    fun i(tag: String, message: String) = println("[PaymentStore] $message")
    fun e(tag: String, message: String) = println("[PaymentStore] ERROR: $message")
}