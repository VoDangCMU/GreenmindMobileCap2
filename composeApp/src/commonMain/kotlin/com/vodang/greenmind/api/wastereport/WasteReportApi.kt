package com.vodang.greenmind.api.wastereport

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

/**
 * Shared DTO for all waste report responses.
 * Fields that differ between create vs get (assignedCollectorId vs assignedTo,
 * presence of imageKey/imageUrl) are all nullable — the JSON config uses
 * ignoreUnknownKeys = true so extra fields are safely discarded.
 */
@Serializable
data class WasteReportDto(
    val id: String,
    val code: String,
    val wasteType: String,
    val wardName: String,
    val lat: Double,
    val lng: Double,
    val wasteKg: Double,
    val description: String,
    val status: String,
    val createdAt: String,
    val imageKey: String? = null,
    val imageUrl: String? = null,
    val assignedCollectorId: String? = null,
    val assignedTo: String? = null,
    val resolvedAt: String? = null,
)

/** Paginated list wrapper used by GET /waste-reports/ and GET /waste-reports/my */
@Serializable
data class WasteReportPageResponse(
    val data: List<WasteReportDto>,
    val total: Int,
    val page: Int,
    val limit: Int,
)

// ── Requests ─────────────────────────────────────────────────────────────────

@Serializable
data class CreateWasteReportRequest(
    val wasteType: String,
    val wardName: String,
    val lat: Double,
    val lng: Double,
    val wasteKg: Double,
    val description: String,
    val imageKey: String,
    val imageUrl: String,
)

@Serializable
data class UpdateWasteReportRequest(
    val wasteType: String,
    val wasteKg: Int,
    val description: String,
    val imageKey: String,
    val imageUrl: String,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** POST /waste-reports — create a new report (response is the DTO directly) */
suspend fun createWasteReport(
    accessToken: String,
    request: CreateWasteReportRequest,
): WasteReportDto {
    AppLogger.i("WasteReport", "createWasteReport wasteType=${request.wasteType} ward=${request.wardName}")
    val resp = httpClient.post("$BASE_URL/waste-reports") {
        header("Authorization", "Bearer $accessToken")
        contentType(ContentType.Application.Json)
        setBody(request)
    }
    return if (resp.status.isSuccess()) {
        resp.body()
    } else {
        val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
        AppLogger.e("WasteReport", "createWasteReport failed: ${resp.status.value} $text")
        throw ApiException(resp.status.value, text)
    }
}

/** GET /waste-reports/ — all reports (admin) */
suspend fun getAllWasteReports(accessToken: String): WasteReportPageResponse {
    AppLogger.i("WasteReport", "getAllWasteReports")
    val resp = httpClient.get("$BASE_URL/waste-reports/") {
        header("Authorization", "Bearer $accessToken")
    }
    return if (resp.status.isSuccess()) {
        resp.body()
    } else {
        val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
        AppLogger.e("WasteReport", "getAllWasteReports failed: ${resp.status.value} $text")
        throw ApiException(resp.status.value, text)
    }
}

/** GET /waste-reports/my — current user's reports */
suspend fun getMyWasteReports(accessToken: String): WasteReportPageResponse {
    AppLogger.i("WasteReport", "getMyWasteReports")
    val resp = httpClient.get("$BASE_URL/waste-reports/my") {
        header("Authorization", "Bearer $accessToken")
    }
    return if (resp.status.isSuccess()) {
        resp.body()
    } else {
        val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
        AppLogger.e("WasteReport", "getMyWasteReports failed: ${resp.status.value} $text")
        throw ApiException(resp.status.value, text)
    }
}

/** GET /waste-reports/{id} */
suspend fun getWasteReportById(accessToken: String, id: String): WasteReportDto {
    AppLogger.i("WasteReport", "getWasteReportById id=$id")
    val resp = httpClient.get("$BASE_URL/waste-reports/$id") {
        header("Authorization", "Bearer $accessToken")
    }
    return if (resp.status.isSuccess()) {
        resp.body()
    } else {
        val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
        AppLogger.e("WasteReport", "getWasteReportById failed: ${resp.status.value} $text")
        throw ApiException(resp.status.value, text)
    }
}

/** PATCH /waste-reports/{id} */
suspend fun updateWasteReport(
    accessToken: String,
    id: String,
    request: UpdateWasteReportRequest,
): WasteReportDto {
    AppLogger.i("WasteReport", "updateWasteReport id=$id")
    val resp = httpClient.patch("$BASE_URL/waste-reports/$id") {
        header("Authorization", "Bearer $accessToken")
        contentType(ContentType.Application.Json)
        setBody(request)
    }
    return if (resp.status.isSuccess()) {
        resp.body()
    } else {
        val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
        AppLogger.e("WasteReport", "updateWasteReport failed: ${resp.status.value} $text")
        throw ApiException(resp.status.value, text)
    }
}

/** DELETE /waste-reports/{id} */
suspend fun deleteWasteReport(accessToken: String, id: String): WasteReportDto {
    AppLogger.i("WasteReport", "deleteWasteReport id=$id")
    val resp = httpClient.delete("$BASE_URL/waste-reports/$id") {
        header("Authorization", "Bearer $accessToken")
    }
    return if (resp.status.isSuccess()) {
        resp.body()
    } else {
        val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
        AppLogger.e("WasteReport", "deleteWasteReport failed: ${resp.status.value} $text")
        throw ApiException(resp.status.value, text)
    }
}
