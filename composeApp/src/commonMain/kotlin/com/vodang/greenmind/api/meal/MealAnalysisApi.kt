package com.vodang.greenmind.api.meal

import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.buildHttpClient
import com.vodang.greenmind.api.auth.ErrorResponse
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
data class PlantAnalysisResponse(
    @SerialName("vegetable_area")           val vegetableArea: Int,
    @SerialName("dish_area")               val dishArea: Double,
    @SerialName("vegetable_ratio_percent") val vegetableRatioPercent: Double,
    @SerialName("plant_image_base64")      val plantImageBase64: String,
    @SerialName("dish_image_base64")       val dishImageBase64: String,
)

// ── App-facing data model ─────────────────────────────────────────────────────

data class MealAnalysisResult(
    val plantRatio: Int,
    val description: String,
    val plantImageBase64: String? = null,
    val dishImageBase64: String? = null,
)

// ── Dedicated HTTP client ─────────────────────────────────────────────────────
// AI inference can be slow — use a longer timeout than the default client.

private val mealAiHttpClient = buildHttpClient(
    tag              = "MealAI",
    requestTimeoutMs = 120_000,
    connectTimeoutMs = 30_000,
    socketTimeoutMs  = 120_000,
)

// ── API call ──────────────────────────────────────────────────────────────────

/**
 * POST /analyze-image-plant — uploads a meal image and returns the plant ratio analysis.
 *
 * @param imageBytes  Raw bytes of the captured image (JPEG).
 * @param filename    File name sent in the multipart form (default: "meal.jpg").
 */
suspend fun analyzeMeal(
    imageBytes: ByteArray,
    filename: String = "meal.jpg",
): MealAnalysisResult {
    AppLogger.i("MealAI", "analyzeMeal filename=$filename bytes=${imageBytes.size}")
    try {
        val response = mealAiHttpClient.post("$AI_BASE_URL/analyze-image-plant") {
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
            AppLogger.e("MealAI", "analyzeMeal failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        val dto = response.body<PlantAnalysisResponse>()
        AppLogger.i("MealAI", "analyzeMeal success ratio=${dto.vegetableRatioPercent}")
        return MealAnalysisResult(
            plantRatio       = dto.vegetableRatioPercent.toInt().coerceIn(0, 100),
            description      = "",
            plantImageBase64 = dto.plantImageBase64,
            dishImageBase64  = dto.dishImageBase64,
        )
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("MealAI", "analyzeMeal error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
