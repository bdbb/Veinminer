package de.miraculixx.veinminerClient.config

import de.miraculixx.veinminerClient.VeinminerClient
import kotlinx.serialization.json.Json
import kotlin.io.path.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object ClientConfigManager {
    private val settingsFile = Path("config/Veinminer/client-settings.json")
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    var settings: ClientSettings = loadSettings()
        private set

    fun save() {
        try {
            settingsFile.createParentDirectories()
            settingsFile.writeText(json.encodeToString(settings))
        } catch (e: Exception) {
            VeinminerClient.LOGGER.warn("Failed to save client settings: ${e.message}")
        }
    }

    private fun loadSettings(): ClientSettings {
        return if (!settingsFile.exists()) {
            settingsFile.createParentDirectories()
            val default = ClientSettings()
            settingsFile.writeText(json.encodeToString(default))
            VeinminerClient.LOGGER.info("Created default client settings")
            default
        } else {
            try {
                json.decodeFromString<ClientSettings>(settingsFile.readText())
            } catch (e: Exception) {
                VeinminerClient.LOGGER.warn("Failed to load client settings: ${e.message}")
                ClientSettings()
            }
        }
    }
}
