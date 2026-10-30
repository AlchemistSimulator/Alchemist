/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.utils

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.nio.file.Files
import java.nio.file.Path

/**
 * Reads the datastore's `inputs` request map from a local JSON file.
 *
 * The map is not accepted as an inline YAML mapping: some datasets require a `type` field,
 * which collides with the Alchemist loader's own `type` keyword used to select
 * nested-parameter implementations, so a JSON file must be used.
 */
internal object CopernicusInputs {

    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Any>>() {}.type

    /**
     * Parses [path] as a JSON object into the request map.
     *
     * JSON objects become nested [Map]s, JSON arrays become [List]s preserving order (since list order is
     * semantic for the datastore API).
     *
     * @param path path to the JSON file (already resolved).
     * @return the parsed request map.
     * @throws JsonSyntaxException if [path] does not hold a valid JSON object.
     */
    fun read(path: Path): Map<String, Any> {
        val json = Files.readString(path)
        val parsed: Map<String, Any> = gson.fromJson(json, mapType)
        return parsed
    }
}
