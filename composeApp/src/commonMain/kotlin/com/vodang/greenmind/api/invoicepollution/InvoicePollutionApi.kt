package com.vodang.greenmind.api.invoicepollution

import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.buildHttpClient
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.api.wastedetect.WasteDetectImpact
import com.vodang.greenmind.api.wastedetect.WasteDetectItem
import com.vodang.greenmind.api.wastedetect.WasteDetectResponse
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

private const val AI_BASE_URL = "https://ai-greenmind.khoav4.com"

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class InvoiceItem(
    @SerialName("raw_name")   val rawName: String,
    val quantity: Int,
    @SerialName("class_id")   val classId: Int,
    @SerialName("class_name") val className: String,
)

@Serializable
data class InvoiceImpact(
    val air: Double,
    val water: Double,
    val soil: Double,
)

@Serializable
data class InvoicePollutionResponse(
    val items: List<InvoiceItem>,
    val pollution: Map<String, Double>,
    val impact: InvoiceImpact,
) {
    val totalItems: Int get() = items.sumOf { it.quantity }

    val activePollutants: List<Pair<String, Double>>
        get() = pollution.entries
            .filter { it.value > 0.0 }
            .sortedByDescending { it.value }
            .map { it.key to it.value }

    val ecoScore: Int
        get() {
            val avg = (impact.air + impact.water + impact.soil) / 3.0
            return ((1.0 - avg) * 100).toInt().coerceIn(0, 100)
        }
}

/** Maps to [WasteDetectResponse] so [EnvImpactCard] can be reused without changes. */
fun InvoicePollutionResponse.toWasteDetectResponse() = WasteDetectResponse(
    items        = items.map { WasteDetectItem(name = it.rawName, quantity = it.quantity, area = 0, className = it.className) },
    totalObjects = totalItems,
    imageUrl     = "",
    pollution    = pollution,
    impact       = WasteDetectImpact(
        airPollution   = impact.air,
        waterPollution = impact.water,
        soilPollution  = impact.soil,
    ),
)

// ── HTTP client ───────────────────────────────────────────────────────────────

private val invoiceAiClient = buildHttpClient(
    tag              = "InvoiceAI",
    requestTimeoutMs = 120_000,
    connectTimeoutMs = 30_000,
    socketTimeoutMs  = 120_000,
)

// ── API call ──────────────────────────────────────────────────────────────────

/**
 * POST /invoice-pollution — OCR a bill image and return detected items with
 * pollution contributions and environmental impact scores.
 */
suspend fun scanInvoicePollution(
    imageBytes: ByteArray,
    filename: String = "bill.jpg",
): InvoicePollutionResponse {
    AppLogger.i("InvoiceAI", "scanInvoicePollution filename=$filename bytes=${imageBytes.size}")
    try {
        val response = invoiceAiClient.post("$AI_BASE_URL/invoice-pollution") {
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
            AppLogger.e("InvoiceAI", "scanInvoicePollution failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        return response.body<InvoicePollutionResponse>().also {
            AppLogger.i("InvoiceAI", "scanInvoicePollution success items=${it.items.size} eco=${it.ecoScore}")
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("InvoiceAI", "scanInvoicePollution error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
