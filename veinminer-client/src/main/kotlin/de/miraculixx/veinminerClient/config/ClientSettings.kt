package de.miraculixx.veinminerClient.config

import kotlinx.serialization.Serializable

@Serializable
data class ClientSettings(
    var toggleMode: Boolean = false
)
