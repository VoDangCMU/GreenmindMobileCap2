package com.vodang.greenmind.api.campaign

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
data class CampaignDto(
    val id: String = "",
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val lat: Double,
    val lng: Double,
    val radius: Int,
    val reportIds: List<String> = emptyList(),
    val status: String? = null,
)

@Serializable
data class CreateCampaignRequest(
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val lat: Double,
    val lng: Double,
    val radius: Int,
    val reportIds: List<String>,
)

@Serializable
data class UpdateCampaignRequest(
    val name: String,
    val description: String,
    val startDate: String,
    val endDate: String,
    val lat: Double,
    val lng: Double,
)

@Serializable
data class UpdateCampaignStatusRequest(
    val status: String,
)

// ── API calls ─────────────────────────────────────────────────────────────────

/** GET /campaigns — returns all campaigns */
suspend fun getAllCampaigns(accessToken: String): List<CampaignDto> {
    AppLogger.i("Campaign", "getAllCampaigns")
    try {
        val resp = httpClient.get("$BASE_URL/campaigns") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "getAllCampaigns → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            AppLogger.e("Campaign", "getAllCampaigns failed: ${resp.status.value} $text")
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "getAllCampaigns error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** GET /campaigns/{id} */
suspend fun getCampaignById(accessToken: String, id: String): CampaignDto {
    AppLogger.i("Campaign", "getCampaignById id=$id")
    try {
        val resp = httpClient.get("$BASE_URL/campaigns/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "getCampaignById → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "getCampaignById error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /campaigns */
suspend fun createCampaign(accessToken: String, request: CreateCampaignRequest): CampaignDto {
    AppLogger.i("Campaign", "createCampaign name=${request.name}")
    try {
        val resp = httpClient.post("$BASE_URL/campaigns") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        AppLogger.d("Campaign", "createCampaign → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "createCampaign error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** PUT /campaigns/{id} */
suspend fun updateCampaign(accessToken: String, id: String, request: UpdateCampaignRequest): CampaignDto {
    AppLogger.i("Campaign", "updateCampaign id=$id")
    try {
        val resp = httpClient.put("$BASE_URL/campaigns/$id") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        AppLogger.d("Campaign", "updateCampaign → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "updateCampaign error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** DELETE /campaigns/{id} */
suspend fun deleteCampaign(accessToken: String, id: String): CampaignDto {
    AppLogger.i("Campaign", "deleteCampaign id=$id")
    try {
        val resp = httpClient.delete("$BASE_URL/campaigns/$id") {
            header("Authorization", "Bearer $accessToken")
        }
        AppLogger.d("Campaign", "deleteCampaign → HTTP ${resp.status.value}")
        return if (resp.status.isSuccess()) {
            resp.body()
        } else {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "deleteCampaign error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}

/** POST /campaigns/{id}/status */
suspend fun updateCampaignStatus(accessToken: String, id: String, status: String) {
    AppLogger.i("Campaign", "updateCampaignStatus id=$id status=$status")
    try {
        val resp = httpClient.post("$BASE_URL/campaigns/$id/status") {
            header("Authorization", "Bearer $accessToken")
            contentType(ContentType.Application.Json)
            setBody(UpdateCampaignStatusRequest(status))
        }
        AppLogger.d("Campaign", "updateCampaignStatus → HTTP ${resp.status.value}")
        if (!resp.status.isSuccess()) {
            val text = try { resp.body<ErrorResponse>().message } catch (_: Throwable) { resp.bodyAsText() }
            throw ApiException(resp.status.value, text)
        }
    } catch (e: ApiException) { throw e }
    catch (e: Throwable) {
        AppLogger.e("Campaign", "updateCampaignStatus error: ${e.message}")
        throw ApiException(0, e.message ?: "Network error")
    }
}
