/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.keyboard

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import javafx.scene.input.KeyCode
import org.kaikikm.threadresloader.ResourceLoader
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Optional

/**
 * Reads and writes a configuration of key bindings to a JSON file.
 */
object Keybinds {
    private const val classpathPath = "it/unibo/alchemist/gui/"
    private const val filename: String = "keybinds.json"
    private val filesystemPath = "${System.getProperty("user.home")}${File.separator}.alchemist${File.separator}"
    private val logger = LoggerFactory.getLogger(Keybinds::class.java)
    private val typeToken: TypeToken<Map<ActionFromKey, KeyCode>> =
        object : TypeToken<Map<ActionFromKey, KeyCode>>() {}
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(typeToken.type, KeybindsSerializer())
        .create()
    private val DEFAULT_CHARSET = Charsets.UTF_8

    /**
     * The currently loaded configuration.
     */
    var config: Map<ActionFromKey, KeyCode> = emptyMap()

    /**
     * Retrieve the keys bound to a certain action.
     */
    operator fun get(action: ActionFromKey): Optional<KeyCode> = Optional.ofNullable(config[action])

    /**
     * Associate an action with a set of keys.
     */
    operator fun set(action: ActionFromKey, key: KeyCode) { config = config + (action to key) }

    /**
     * Write the binds to the file system.
     * @throws IOException
     */
    @Throws(IOException::class)
    fun save() {
        File("$filesystemPath$filename").apply {
            val parentOK = parentFile.exists() || parentFile.mkdirs()
            val fileIsAvailable = parentOK && (exists() || createNewFile())
            if (!fileIsAvailable) {
                throw IOException("Failed to create keybind configuration file")
            }
            FileWriter(path, DEFAULT_CHARSET).use { w -> w.write(gson.toJson(config, typeToken.type)) }
        }
    }

    /**
     * Load the binds from a file in the file system, reverting to classpath on failure.
     */
    fun load() {
        if (loadFromFile().not()) {
            loadFromClasspath()
        }
    }

    /**
     * Attempt to load the binds from the filesystem.
     */
    private fun loadFromFile(): Boolean {
        val tryToLoad = runCatching {
            config = gson.fromJson(
                File("$filesystemPath$filename").readText(),
                typeToken.type,
            )
        }
        return tryToLoad.map { true }
            .onFailure { logger.warn("Could not load key bindings from file", it) }
            .getOrDefault(false)
    }

    /**
     * Read the binds from classpath.
     */
    private fun loadFromClasspath() {
        config = gson.fromJson(
            ResourceLoader.getResource("$classpathPath$filename").readText(DEFAULT_CHARSET),
            typeToken.type,
        )
    }
}
