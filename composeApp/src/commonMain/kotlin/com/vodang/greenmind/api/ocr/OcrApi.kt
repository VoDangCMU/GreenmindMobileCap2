package com.vodang.greenmind.api.ocr

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.buildHttpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ── DTOs ─────────────────────────────────────────────────────────────────────
// All fields are nullable — the OCR model may omit any field it cannot extract.

@Serializable
data class OcrResponse(
    val doc: OcrDoc? = null,
    val vendor: OcrVendor? = null,
    val datetime: OcrDatetime? = null,
    val items: List<OcrItem>? = null,
    val totals: OcrTotals? = null,
)

@Serializable
data class OcrDoc(
    @SerialName("source_id")     val sourceId: String? = null,
    val currency: String? = null,
    @SerialName("payment_method") val paymentMethod: String? = null,
    val notes: String? = null,
)

@Serializable
data class OcrVendor(
    val name: String? = null,
    val address: String? = null,
    @SerialName("geo_hint") val geoHint: String? = null,
)

@Serializable
data class OcrDatetime(
    val date: String? = null,
    val time: String? = null,
)

@Serializable
data class OcrItem(
    @SerialName("raw_name")              val rawName: String? = null,
    val brand: String? = null,
    val category: String? = null,
    @SerialName("plant_based")           val plantBased: Boolean? = null,
    val quantity: Int? = null,
    @SerialName("unit_price")            val unitPrice: Double? = null,
    @SerialName("line_total")            val lineTotal: Double? = null,
    @SerialName("matched_shopping_list") val matchedShoppingList: Boolean? = null,
)

@Serializable
data class OcrTotals(
    val subtotal: Double? = null,
    val discount: Double? = null,
    val tax: Double? = null,
    @SerialName("grand_total") val grandTotal: Double? = null,
)

// ── Invoice list DTOs ─────────────────────────────────────────────────────────
// GET /ocr/invoices returns totals as strings ("8.0") instead of numbers.
// InvoiceTotals uses String? fields to match this shape; parse with toDoubleOrNull().

@Serializable
data class InvoiceTotals(
    val subtotal: String? = null,
    val discount: String? = null,
    val tax: String? = null,
    @SerialName("grand_total") val grandTotal: String? = null,
) {
    fun grandTotalDouble(): Double = grandTotal?.toDoubleOrNull() ?: 0.0
    fun subtotalDouble(): Double  = subtotal?.toDoubleOrNull()  ?: 0.0
    fun discountDouble(): Double  = discount?.toDoubleOrNull()  ?: 0.0
    fun taxDouble(): Double       = tax?.toDoubleOrNull()       ?: 0.0
}

@Serializable
data class InvoiceDto(
    val id: String,
    val doc: OcrDoc? = null,
    val vendor: OcrVendor? = null,
    val datetime: OcrDatetime? = null,
    val items: List<OcrItem>? = null,
    val totals: InvoiceTotals? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
) {
    /** Plant-based line totals / grand total × 100, clamped 0-100. */
    fun greenRatio(): Int {
        val grand = totals?.grandTotalDouble() ?: 0.0
        if (grand <= 0.0) return 0
        val plant = items?.filter { it.plantBased == true }?.sumOf { it.lineTotal ?: 0.0 } ?: 0.0
        return ((plant / grand) * 100).toInt().coerceIn(0, 100)
    }
}

// ── Dedicated HTTP client ─────────────────────────────────────────────────────
// OCR inference is slow — use a much longer timeout than the default client.

private val ocrHttpClient = buildHttpClient(
    tag              = "OCR",
    requestTimeoutMs = 120_000,
    connectTimeoutMs = 30_000,
    socketTimeoutMs  = 120_000,
)

// ── API call ──────────────────────────────────────────────────────────────────

/**
 * POST /ocr — uploads a bill image and returns the structured OCR result.
 *
 * @param accessToken  Bearer token for the backend.
 * @param imageBytes   Raw bytes of the captured image (JPEG).
 * @param filename     File name sent in the multipart form (default: "bill.jpg").
 */
suspend fun ocrBill(
    accessToken: String,
    imageBytes: ByteArray,
    filename: String = "bill.jpg",
): OcrResponse {
    AppLogger.i("OCR", "ocrBill filename=$filename bytes=${imageBytes.size}")
    try {
        val response = ocrHttpClient.post("$BASE_URL/ocr") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(MultiPartFormDataContent(formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
            }))
        }
        if (!response.status.isSuccess()) {
            val msg = try { response.body<ErrorResponse>().message }
                      catch (_: Throwable) { response.bodyAsText() }
            AppLogger.e("OCR", "ocrBill failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        return response.body<OcrResponse>().also {
            AppLogger.i("OCR", "ocrBill success vendor=${it.vendor?.name} items=${it.items?.size}")
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("OCR", "ocrBill error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

private const val AI_OCR_BASE_URL = "https://ai-greenmind.khoav4.com"

/**
 * POST /ocr_text — AI bill OCR endpoint (no auth required).
 * Returns the same [OcrResponse] shape as [ocrBill] but runs on the AI server.
 *
 * @param imageBytes  Raw bytes of the captured image (JPEG).
 * @param filename    File name sent in the multipart form (default: "bill.jpg").
 */
suspend fun aiOcrBill(
    imageBytes: ByteArray,
    filename: String = "bill.jpg",
): OcrResponse {
    AppLogger.i("OCR", "aiOcrBill filename=$filename bytes=${imageBytes.size}")
    try {
        val response = ocrHttpClient.post("$AI_OCR_BASE_URL/ocr_text") {
            setBody(MultiPartFormDataContent(formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
            }))
        }
        if (!response.status.isSuccess()) {
            val msg = try { response.body<ErrorResponse>().message }
                      catch (_: Throwable) { response.bodyAsText() }
            AppLogger.e("OCR", "aiOcrBill failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        return response.body<OcrResponse>().also {
            AppLogger.i("OCR", "aiOcrBill success vendor=${it.vendor?.name} items=${it.items?.size}")
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("OCR", "aiOcrBill error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/**
 * GET /ocr/invoices — fetches the user's full bill-scan history from the server.
 * Uses the standard [httpClient] (normal timeout; this is just a JSON list fetch).
 */
suspend fun getInvoices(accessToken: String): List<InvoiceDto> {
    AppLogger.i("OCR", "getInvoices")
    try {
        val response = httpClient.get("$BASE_URL/ocr/invoices") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        if (!response.status.isSuccess()) {
            val msg = try { response.body<ErrorResponse>().message }
                      catch (_: Throwable) { response.bodyAsText() }
            AppLogger.e("OCR", "getInvoices failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        return response.body<List<InvoiceDto>>().also {
            AppLogger.i("OCR", "getInvoices loaded ${it.size} invoices")
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("OCR", "getInvoices error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

// ── Invoice Pollution (OCR & Pollutant Analyst) ──────────────────────────────

@Serializable
data class InvoicePollutionItem(
    @SerialName("raw_name") val rawName: String,
    val quantity: Int,
    @SerialName("class_id") val classId: Int,
    @SerialName("class_name") val className: String,
)

@Serializable
data class InvoicePollutionImpact(
    val air: Int,
    val water: Int,
    val soil: Int,
)

@Serializable
data class InvoicePollutionResponse(
    val items: List<InvoicePollutionItem>,
    val pollution: Map<String, Int>,
    val impact: InvoicePollutionImpact,
)

/**
 * POST /invoice-pollution — OCR Bill & Pollutant Analyst endpoint.
 *
 * @param imageBytes  Raw bytes of the captured image (JPEG).
 * @param filename    File name sent in the multipart form (default: "bill.jpg").
 */
suspend fun analyzeInvoicePollution(
    imageBytes: ByteArray,
    filename: String = "bill.jpg",
): InvoicePollutionResponse {
    AppLogger.i("OCR", "analyzeInvoicePollution filename=$filename bytes=${imageBytes.size}")
    try {
        val response = ocrHttpClient.post("$AI_OCR_BASE_URL/invoice-pollution") {
            setBody(MultiPartFormDataContent(formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
            }))
        }
        if (!response.status.isSuccess()) {
            val msg = try { response.body<ErrorResponse>().message }
                      catch (_: Throwable) { response.bodyAsText() }
            AppLogger.e("OCR", "analyzeInvoicePollution failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        return response.body<InvoicePollutionResponse>().also {
            AppLogger.i("OCR", "analyzeInvoicePollution success items=${it.items.size}")
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("OCR", "analyzeInvoicePollution error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
