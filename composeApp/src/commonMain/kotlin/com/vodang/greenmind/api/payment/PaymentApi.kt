package com.vodang.greenmind.api.payment

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── DTOs ─────────────────────────────────────────────────────────────────────

/**
 * Invoice status enum.
 * Values: PENDING, PAID, OVERDUE, CANCELLED
 */
@Serializable
data class InvoiceStatusDto(
    val status: String,
    val label: String,
)

/**
 * Invoice item line.
 */
@Serializable
data class InvoiceItemDto(
    val id: String,
    val name: String,
    val description: String? = null,
    val quantity: Int = 1,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("line_total") val lineTotal: Double,
)

/**
 * Main invoice DTO.
 */
@Serializable
data class InvoiceDto(
    val id: String,
    val code: String,
    val title: String,
    val description: String? = null,
    val amount: Double,
    @SerialName("amount_paid") val amountPaid: Double = 0.0,
    val currency: String = "VND",
    val status: String, // PENDING | PAID | OVERDUE | CANCELLED
    @SerialName("due_date") val dueDate: String,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
    val items: List<InvoiceItemDto> = emptyList(),
    @SerialName("stripe_payment_intent_id") val stripePaymentIntentId: String? = null,
    @SerialName("stripe_client_secret") val stripeClientSecret: String? = null,
)

/**
 * Payment intent response from backend after creating Stripe payment intent.
 */
@Serializable
data class PaymentIntentResponse(
    val id: String,
    @SerialName("client_secret") val clientSecret: String,
    @SerialName("publishable_key") val publishableKey: String,
    val amount: Double,
    val currency: String,
    val status: String,
)

/**
 * Payment record/history item.
 */
@Serializable
data class PaymentRecordDto(
    val id: String,
    @SerialName("invoice_id") val invoiceId: String,
    @SerialName("invoice_code") val invoiceCode: String,
    val amount: Double,
    val currency: String = "VND",
    val status: String, // SUCCEEDED | FAILED | REFUNDED
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("paid_at") val paidAt: String? = null,
    @SerialName("created_at") val createdAt: String,
)

/**
 * Paginated invoice list response.
 */
@Serializable
data class InvoiceListResponse(
    val message: String? = null,
    val data: List<InvoiceDto>,
    val total: Int = 0,
    val page: Int = 1,
    val limit: Int = 20,
)

/**
 * Payment history response.
 */
@Serializable
data class PaymentHistoryResponse(
    val message: String? = null,
    val data: List<PaymentRecordDto>,
    val total: Int = 0,
)

/**
 * Create payment intent request.
 */
@Serializable
data class CreatePaymentIntentRequest(
    val invoiceId: String,
)

/**
 * Confirm payment request.
 */
@Serializable
data class ConfirmPaymentRequest(
    @SerialName("payment_intent_id") val paymentIntentId: String,
    @SerialName("payment_method_id") val paymentMethodId: String? = null,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/**
 * GET /payments/invoices
 *
 * TODO: Backend implementation needed
 * Fetches all invoices for the current user (paid and unpaid).
 *
 * @param accessToken JWT Bearer token
 * @param status Optional filter: PENDING | PAID | OVERDUE | CANCELLED
 * @param page Page number (default 1)
 * @param limit Items per page (default 20)
 */
suspend fun getInvoices(
    accessToken: String,
    status: String? = null,
    page: Int = 1,
    limit: Int = 20,
): InvoiceListResponse {
    AppLogger.i("Payment", "getInvoices status=$status page=$page")
    // TODO: Replace with actual API call
    // try {
    //     val resp = httpClient.get("$BASE_URL/payments/invoices") {
    //         header("Authorization", "Bearer $accessToken")
    //         parameter("status", status)
    //         parameter("page", page)
    //         parameter("limit", limit)
    //     }
    //     return if (resp.status.isSuccess()) resp.body() else throw ApiException(resp.status.value, resp.bodyAsText())
    // } catch (e: Throwable) { throw ApiException(0, e.message ?: "Network error") }
    throw ApiException(0, "TODO: Implement getInvoices API call")
}

/**
 * GET /payments/invoices/{id}
 *
 * TODO: Backend implementation needed
 * Fetches a single invoice by ID.
 *
 * @param accessToken JWT Bearer token
 * @param invoiceId The invoice ID
 */
suspend fun getInvoiceById(
    accessToken: String,
    invoiceId: String,
): InvoiceDto {
    AppLogger.i("Payment", "getInvoiceById invoiceId=$invoiceId")
    // TODO: Replace with actual API call
    // try {
    //     val resp = httpClient.get("$BASE_URL/payments/invoices/$invoiceId") {
    //         header("Authorization", "Bearer $accessToken")
    //     }
    //     return if (resp.status.isSuccess()) resp.body() else throw ApiException(resp.status.value, resp.bodyAsText())
    // } catch (e: Throwable) { throw ApiException(0, e.message ?: "Network error") }
    throw ApiException(0, "TODO: Implement getInvoiceById API call")
}

/**
 * POST /payments/create-intent
 *
 * TODO: Backend implementation needed
 * Creates a Stripe PaymentIntent for the given invoice.
 * Returns client_secret needed for Stripe SDK confirmation on client side.
 *
 * @param accessToken JWT Bearer token
 * @param request CreatePaymentIntentRequest with invoiceId
 */
suspend fun createPaymentIntent(
    accessToken: String,
    request: CreatePaymentIntentRequest,
): PaymentIntentResponse {
    AppLogger.i("Payment", "createPaymentIntent invoiceId=${request.invoiceId}")
    // TODO: Replace with actual API call
    // try {
    //     val resp = httpClient.post("$BASE_URL/payments/create-intent") {
    //         header("Authorization", "Bearer $accessToken")
    //         contentType(ContentType.Application.Json)
    //         setBody(request)
    //     }
    //     return if (resp.status.isSuccess()) resp.body() else throw ApiException(resp.status.value, resp.bodyAsText())
    // } catch (e: Throwable) { throw ApiException(0, e.message ?: "Network error") }
    throw ApiException(0, "TODO: Implement createPaymentIntent API call")
}

/**
 * POST /payments/confirm
 *
 * TODO: Backend implementation needed
 * Confirms a payment was completed (webhook callback from Stripe or client-side confirmation).
 *
 * @param accessToken JWT Bearer token
 * @param request ConfirmPaymentRequest with paymentIntentId
 */
suspend fun confirmPayment(
    accessToken: String,
    request: ConfirmPaymentRequest,
): PaymentRecordDto {
    AppLogger.i("Payment", "confirmPayment paymentIntentId=${request.paymentIntentId}")
    // TODO: Replace with actual API call
    // try {
    //     val resp = httpClient.post("$BASE_URL/payments/confirm") {
    //         header("Authorization", "Bearer $accessToken")
    //         contentType(ContentType.Application.Json)
    //         setBody(request)
    //     }
    //     return if (resp.status.isSuccess()) resp.body() else throw ApiException(resp.status.value, resp.bodyAsText())
    // } catch (e: Throwable) { throw ApiException(0, e.message ?: "Network error") }
    throw ApiException(0, "TODO: Implement confirmPayment API call")
}

/**
 * GET /payments/history
 *
 * TODO: Backend implementation needed
 * Returns the payment history (all successful payments).
 *
 * @param accessToken JWT Bearer token
 * @param page Page number (default 1)
 * @param limit Items per page (default 20)
 */
suspend fun getPaymentHistory(
    accessToken: String,
    page: Int = 1,
    limit: Int = 20,
): PaymentHistoryResponse {
    AppLogger.i("Payment", "getPaymentHistory page=$page")
    // TODO: Replace with actual API call
    // try {
    //     val resp = httpClient.get("$BASE_URL/payments/history") {
    //         header("Authorization", "Bearer $accessToken")
    //         parameter("page", page)
    //         parameter("limit", limit)
    //     }
    //     return if (resp.status.isSuccess()) resp.body() else throw ApiException(resp.status.value, resp.bodyAsText())
    // } catch (e: Throwable) { throw ApiException(0, e.message ?: "Network error") }
    throw ApiException(0, "TODO: Implement getPaymentHistory API call")
}