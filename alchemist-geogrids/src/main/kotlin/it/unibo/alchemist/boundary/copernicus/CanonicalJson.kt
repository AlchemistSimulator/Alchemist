/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.copernicus

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import it.unibo.alchemist.util.KClasses.nonNullSimpleName
import java.util.TreeMap

/**
 * Encodes JSON with the keys of every object sorted, so equal requests encode the same way.
 * Arrays keep their order, which matters to the data stores.
 */
internal object CanonicalJson {

    private val gson: Gson = GsonBuilder().serializeNulls().create()

    /**
     * [value], typically nested [Map]s and [List]s, as JSON.
     *
     * @throws IllegalArgumentException if [value] has no JSON equivalent (see [toJson]).
     */
    fun encode(value: Any): String = gson.toJson(toJson(value))

    /**
     * [value], found at [path], as JSON with the keys of every object sorted.
     * [Map]s become objects, and [Iterable]s and [Array]s become arrays.
     *
     * @throws IllegalArgumentException if [value] holds non-string keys, or values with no JSON equivalent.
     */
    fun toJson(value: Any?, path: String = "$"): JsonElement = when (value) {
        null -> JsonNull.INSTANCE
        is Boolean -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        is Map<*, *> -> JsonObject().also { json ->
            value.entries
                .associateTo(TreeMap()) { (key, entry) ->
                    requireNotNull(key as? String) {
                        "The key $key of $path is not a string, so it is not a valid JSON key"
                    } to entry
                }
                .forEach { (key, entry) -> json.add(key, toJson(entry, "$path.$key")) }
        }
        is Iterable<*> -> value.toJsonArray(path)
        is Array<*> -> value.asIterable().toJsonArray(path)
        else -> throw IllegalArgumentException(
            "$path is a ${value::class.nonNullSimpleName} ($value), which has no JSON equivalent",
        )
    }

    private fun Iterable<*>.toJsonArray(path: String): JsonArray = JsonArray().also { json ->
        forEachIndexed { index, element -> json.add(toJson(element, "$path[$index]")) }
    }
}
