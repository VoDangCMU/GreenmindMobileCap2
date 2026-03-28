package com.vodang.greenmind.api

import com.vodang.greenmind.store.NetworkCaptureStore
import com.vodang.greenmind.store.NetworkEntry
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.time.TimeSource

const val BASE_URL = "https://vodang-api.gauas.com"

val httpClient: HttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
        })
    }
    install(Logging) {
        logger = object : Logger {
            override fun log(message: String) {
                AppLogger.d("HTTP", message)
            }
        }
        level = LogLevel.HEADERS
    }
    install(HttpCallValidator) {
        validateResponse { response ->
            val method = response.call.request.method.value
            val url = response.call.request.url.encodedPath
            val status = response.status.value
            AppLogger.i("API", "$method $url → $status")
        }
    }
}.also { client ->
    client.plugin(HttpSend).intercept { request ->
        val mark = TimeSource.Monotonic.markNow()

        val reqBody = try {
            when (val b = request.body) {
                is OutgoingContent.ByteArrayContent -> {
                    val ct = b.contentType?.toString() ?: ""
                    if (ct.startsWith("image/") || ct.startsWith("application/octet")) {
                        "[binary ${b.bytes().size} bytes — $ct]"
                    } else {
                        b.bytes().decodeToString()
                    }
                }
                is OutgoingContent.WriteChannelContent -> {
                    val ct = b.contentType?.toString() ?: "stream"
                    "[${ct}]"
                }
                is OutgoingContent.ReadChannelContent -> {
                    val ct = b.contentType?.toString() ?: "stream"
                    "[${ct}]"
                }
                is OutgoingContent.NoContent -> ""
                else -> ""
            }
        } catch (_: Throwable) { "" }

        val reqHeaders = try {
            request.headers.entries().map { (k, v) -> k to v.joinToString(", ") }
        } catch (_: Throwable) { emptyList() }

        val call = execute(request)
        val durationMs = mark.elapsedNow().inWholeMilliseconds
        val savedCall = call.save()

        val respBody = try { savedCall.response.bodyAsText() } catch (_: Throwable) { "" }
        val respHeaders = try {
            savedCall.response.headers.entries().map { (k, v) -> k to v.joinToString(", ") }
        } catch (_: Throwable) { emptyList() }

        NetworkCaptureStore.add(
            NetworkEntry(
                method = request.method.value,
                url = request.url.toString(),
                statusCode = savedCall.response.status.value,
                durationMs = durationMs,
                requestHeaders = reqHeaders,
                requestBody = reqBody,
                responseHeaders = respHeaders,
                responseBody = respBody,
            )
        )

        savedCall
    }
}
