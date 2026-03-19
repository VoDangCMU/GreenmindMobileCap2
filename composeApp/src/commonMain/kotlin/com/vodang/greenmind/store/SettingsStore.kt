package com.vodang.greenmind.store

import com.russhwolf.settings.Settings
import com.vodang.greenmind.api.auth.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val KEY_ACCESS_TOKEN = "access_token"
private const val KEY_REFRESH_TOKEN = "refresh_token"
private const val KEY_USER = "user"
private const val KEY_LANGUAGE = "language"

object SettingsStore {

    private val settings: Settings = Settings()
    private val json = Json { ignoreUnknownKeys = true }

    // Initialise flows from persisted values so they survive app restarts
    private val _accessToken = MutableStateFlow<String?>(settings.getStringOrNull(KEY_ACCESS_TOKEN))
    private val _refreshToken = MutableStateFlow<String?>(settings.getStringOrNull(KEY_REFRESH_TOKEN))
    private val _user = MutableStateFlow<UserDto?>(
        settings.getStringOrNull(KEY_USER)
            ?.let { runCatching { json.decodeFromString<UserDto>(it) }.getOrNull() }
    )
    private val _language = MutableStateFlow(settings.getStringOrNull(KEY_LANGUAGE) ?: "vi")

    val accessToken: StateFlow<String?> = _accessToken.asStateFlow()
    val refreshToken: StateFlow<String?> = _refreshToken.asStateFlow()
    val user: StateFlow<UserDto?> = _user.asStateFlow()
    val language: StateFlow<String> = _language.asStateFlow()

    fun getAccessToken(): String? = _accessToken.value
    fun getRefreshToken(): String? = _refreshToken.value
    fun getUser(): UserDto? = _user.value

    fun setAccessToken(token: String?) {
        if (token == null) settings.remove(KEY_ACCESS_TOKEN) else settings.putString(KEY_ACCESS_TOKEN, token)
        _accessToken.value = token
    }

    fun setRefreshToken(token: String?) {
        if (token == null) settings.remove(KEY_REFRESH_TOKEN) else settings.putString(KEY_REFRESH_TOKEN, token)
        _refreshToken.value = token
    }

    fun setUser(userDto: UserDto?) {
        if (userDto == null) settings.remove(KEY_USER) else settings.putString(KEY_USER, json.encodeToString(userDto))
        _user.value = userDto
    }

    fun setLanguage(lang: String) {
        settings.putString(KEY_LANGUAGE, lang)
        _language.value = lang
    }

    fun clearAll() {
        settings.remove(KEY_ACCESS_TOKEN)
        settings.remove(KEY_REFRESH_TOKEN)
        settings.remove(KEY_USER)
        _accessToken.value = null
        _refreshToken.value = null
        _user.value = null
    }
}
