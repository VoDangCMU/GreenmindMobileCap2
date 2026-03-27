package com.vodang.greenmind.api.restcountries

import com.vodang.greenmind.api.httpClient
import com.vodang.greenmind.util.AppLogger
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable

private const val REST_COUNTRIES_BASE = "https://restcountries.com/v3.1"

@Serializable
data class CountryName(
    val common: String,
    val official: String = common,
)

@Serializable
data class CountryDto(
    val name: CountryName,
    val flag: String = "",
    val cca2: String,
)

private var cachedCountries: List<CountryDto>? = null

private suspend inline fun <reified T> checkResponse(resp: HttpResponse): T =
    if (resp.status.isSuccess()) resp.body()
    else throw Exception("RestCountries ${resp.status.value}: ${resp.bodyAsText()}")

suspend fun getAllCountries(): List<CountryDto> {
    cachedCountries?.let { return it }
    AppLogger.i("Countries", "getAllCountries")
    val resp = httpClient.get("$REST_COUNTRIES_BASE/all") {
        parameter("fields", "name,flag,cca2")
    }
    val result = checkResponse<List<CountryDto>>(resp).sortedBy { it.name.common }
    cachedCountries = result
    return result
}
