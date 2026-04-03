package com.vodang.greenmind.api.wastecollector

import com.vodang.greenmind.api.BASE_URL
import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.api.auth.ApiException
import com.vodang.greenmind.api.auth.ErrorResponse
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

// ── DTOs ─────────────────────────────────────────────────────────────────────

@Serializable
data class WasteCollectorReportDto(
    val id: String,
    val code: String,
    val reportedBy: String,
    val lat: Double,
    val lng: Double,
    val wasteKg: Double,
    val wasteType: String,
    val description: String,
    val status: String,
    val createdAt: String,
    val resolvedAt: String? = null,
)

@Serializable
data class WasteCollectorListResponse(
    val data: List<WasteCollectorReportDto>,
    val total: Int,
)

@Serializable
data class WasteCollectorReportDetailDto(
    val id: String,
    val code: String,
    val description: String,
    val imageKey: String,
    val imageUrl: String,
    val lat: Double,
    val lng: Double,
    val wasteKg: Int,
    val wasteType: String,
    val wardName: String,
    val status: String,
    val reportedByUserId: String,
    val assignedCollectorId: String,
    val imageEvidenceUrl: String? = null,
    val createdAt: String,
    val resolvedAt: String? = null,
)

@Serializable
data class UpdateStatusRequest(
    val status: String,
    val imageEvidenceUrl: String,
)

@Serializable
data class UpdateStatusResponse(
    val id: String,
    val code: String,
    val status: String,
    val wardName: String,
    val reportedByUserId: String,
    val reportedBy: String,
    val collectorId: String,
    val imageEvidenceUrl: String,
    val resolvedAt: String,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** GET /waste-collector — all reports assigned to the current collector */
suspend fun getAssignedReports(accessToken: String): WasteCollectorListResponse {
    AppLogger.i("WasteCollector", "getAssignedReports")
    try {
        val resp = httpClient.get("$BASE_URL/waste-collector") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteCollector", "getAssignedReports → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteCollector", "getAssignedReports failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteCollector", "getAssignedReports error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /waste-collector/{id} — detail of one assigned report */
suspend fun getAssignedReportById(accessToken: String, id: String): WasteCollectorReportDetailDto {
    AppLogger.i("WasteCollector", "getAssignedReportById id=$id")
    try {
        val resp = httpClient.get("$BASE_URL/waste-collector/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("WasteCollector", "getAssignedReportById → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteCollector", "getAssignedReportById failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteCollector", "getAssignedReportById error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** PATCH /waste-collector/{id}/status — update collection status with evidence image */
suspend fun updateReportStatus(
    accessToken: String,
    id: String,
    request: UpdateStatusRequest,
): UpdateStatusResponse {
    AppLogger.i("WasteCollector", "updateReportStatus id=$id status=${request.status}")
    try {
        val resp = httpClient.patch("$BASE_URL/waste-collector/$id/status") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        AppLogger.d("WasteCollector", "updateReportStatus → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("WasteCollector", "updateReportStatus failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("WasteCollector", "updateReportStatus error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
