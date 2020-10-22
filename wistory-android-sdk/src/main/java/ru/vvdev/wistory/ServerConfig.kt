package ru.vvdev.wistory

internal data class ServerConfig(
    val token: String?,
    val serverUrl: String?,
    val registrationId: String? = null
)
