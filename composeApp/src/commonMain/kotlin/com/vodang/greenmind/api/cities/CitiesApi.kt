package com.vodang.greenmind.api.cities

import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

private const val CITIES_BASE = "https://countriesnow.space/api/v0.1"

@Serializable
private data class CitiesResponse(
    val error: Boolean,
    val msg: String = "",
    val data: List<String> = emptyList(),
)

// Simple in-memory cache: country name → sorted city list
private val citiesCache = mutableMapOf<String, List<String>>()

suspend fun getCitiesByCountry(countryName: String): List<String> {
    val key = countryName.lowercase()
    citiesCache[key]?.let { return it }

    AppLogger.i("Cities", "getCitiesByCountry country=$countryName")
    val resp = httpClient.get("$CITIES_BASE/countries/cities/q") {
        parameter("country", countryName)
    }
    if (!resp.status.isSuccess()) throw Exception("Cities API ${resp.status.value}: ${resp.bodyAsText()}")

    val body = resp.body<CitiesResponse>()
    if (body.error) throw Exception("Cities API error: ${body.msg}")

    val result = body.data.sorted()
    citiesCache[key] = result
    return result
}
