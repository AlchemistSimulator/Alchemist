/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.input

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Optional
import javafx.scene.input.KeyCode
import org.kaikikm.threadresloader.ResourceLoader
import java.lang.reflect.Type

/**
 * Actions which can be bound to a key on the keyboard.
 */
enum class ActionFromKey(private val description: String) {
    MODIFIER_CONTROL("Control modifier"),
    MODIFIER_SHIFT("Shift modifier"),
    PAN_NORTH("Pan north"),
    PAN_SOUTH("Pan south"),
    PAN_EAST("Pan east"),
    PAN_WEST("Pan west"),
    DELETE("Delete"),
    MOVE("Move"),
    EDIT("Edit"),
    PLAY_AND_PAUSE("Play and Pause"),
    ONE_STEP("Forward one step");

    override fun toString() = description
}

/**
 * Serializer for keybinds that serializes [ActionFromKey]
 * by using the enum values' names instead of [ActionFromKey.toString].
 */
class KeybindsSerializer : JsonSerializer<Map<ActionFromKey, KeyCode>> {
    /**
     * {@inheritDoc}.
     */
    override fun serialize(src: Map<ActionFromKey, KeyCode>, typeOfSrc: Type, context: JsonSerializationContext) =
        JsonObject().apply {
            src.forEach { addProperty(it.key.name, it.value.toString()) }
        }
}

/**
 * Reads and writes a configuration of key bindings to a JSON file.
 */
class Keybinds private constructor() {
    companion object {
        private val filesystemPath = "${System.getProperty("user.home")}${File.separator}.alchemist${File.separator}"
        private const val classpathPath = "it/unibo/alchemist/gui/"
        private const val filename: String = "keybinds.json"
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
        private fun loadFromFile(): Boolean =
            try {
                config = gson.fromJson(
                    File("$filesystemPath$filename").readLines(DEFAULT_CHARSET).reduce { a, b -> a + b },
                    typeToken.type
                )
                true
            } catch (e: IOException) {
                false
            }

        /**
         * Read the binds from classpath.
         */
        private fun loadFromClasspath() {
            config = gson.fromJson(
                ResourceLoader.getResource("$classpathPath$filename").readText(DEFAULT_CHARSET),
                typeToken.type
            )
        }
    }
}
