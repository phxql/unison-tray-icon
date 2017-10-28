package de.mkammerer.unisontray.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.nio.file.Files
import java.nio.file.Paths

data class Config(val syncInterval: Int, val profile: String) {
    companion object {
        val DEFAULT = Config(5, "default")
    }
}

interface ConfigManager {
    fun load(): Config

    fun save(config: Config)
}

class ConfigManagerImpl : ConfigManager {
    private val mapper = ObjectMapper()
            .registerModule(KotlinModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(SerializationFeature.INDENT_OUTPUT)

    private val configPath = Paths.get(CONFIG_LOCATION.replace("<HOME>", System.getProperty("user.home")))

    override fun load(): Config {
        if (!Files.exists(configPath)) return Config.DEFAULT

        return Files.newInputStream(configPath).use { it -> mapper.readValue(it, Config::class.java) }
    }

    override fun save(config: Config) {
        Files.createDirectories(configPath.parent)

        Files.newOutputStream(configPath).use {
            mapper.writeValue(it, config)
        }
    }

    companion object {
        const val CONFIG_LOCATION = "<HOME>/.unison-tray.config"
    }
}