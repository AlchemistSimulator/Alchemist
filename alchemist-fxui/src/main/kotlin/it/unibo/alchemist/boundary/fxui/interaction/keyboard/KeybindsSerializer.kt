/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.keyboard

import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import javafx.scene.input.KeyCode
import java.lang.reflect.Type

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
