package com.vodang.greenmind.api.wastesort

import com.vodang.greenmind.api.auth.ApiException
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

@Serializable
data class DetectTrashResponse(
    @SerialName("total_objects") val totalObjects: Int,
    @SerialName("image_url")     val imageUrl: String,
    val grouped: Map<String, List<String>>,
)

private val detectHttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true; isLenient = true })
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 120_000 // 2 min — AI inference can be slow
        connectTimeoutMillis = 30_000
        socketTimeoutMillis  = 120_000
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) { AppLogger.d("DetectTrash", message) }
        }
        level = LogLevel.INFO
    }
}

/**
 * POST /detect-trash-ver2 — uploads an image and returns the waste detection result.
 *
 * @param imageBytes  Raw bytes of the captured image (JPEG).
 * @param filename    File name sent in the multipart form (default: "scan.jpg").
 */
suspend fun detectTrash(
    imageBytes: ByteArray,
    filename: String = "scan.jpg",
): DetectTrashResponse {
    AppLogger.i("DetectTrash", "detectTrash filename=$filename bytes=${imageBytes.size}")
    try {
        val response = detectHttpClient.post("$AI_BASE_URL/detect-trash-ver2") {
            setBody(MultiPartFormDataContent(formData {
                append("file", imageBytes, Headers.build {
                    append(HttpHeaders.ContentType, "image/jpeg")
                    append(HttpHeaders.ContentDisposition, "filename=\"$filename\"")
                })
            }))
        }
        if (!response.status.isSuccess()) {
            val msg = try { response.bodyAsText() } catch (_: Throwable) { response.status.description }
            AppLogger.e("DetectTrash", "detectTrash failed: ${response.status.value} $msg")
            throw ApiException(response.status.value, msg)
        }
        return response.body<DetectTrashResponse>().also {
            AppLogger.i("DetectTrash", "detectTrash success totalObjects=${it.totalObjects}")
        }
    } catch (e: ApiException) {
        throw e
    } catch (e: Throwable) {
        AppLogger.e("DetectTrash", "detectTrash error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
