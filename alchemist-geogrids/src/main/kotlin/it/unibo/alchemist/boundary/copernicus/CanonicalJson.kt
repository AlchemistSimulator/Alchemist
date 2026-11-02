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
import java.util.TreeMap

/**
 * Encodes JSON with the keys of every object sorted, so equal requests encode the same way.
 * Lists keep their order, which matters to the data stores.
 */
internal object CanonicalJson {

    private val gson: Gson = GsonBuilder().serializeNulls().create()

    /**
     * [value], typically nested [Map]s and [List]s, as JSON.
     */
    fun encode(value: Any): String = gson.toJson(canonicalize(value))

    /**
     * [value] with every [Map] in it sorted by key.
     */
    private fun canonicalize(value: Any?): Any? = when (value) {
        is Map<*, *> -> TreeMap<String, Any?>().apply {
            value.forEach { (key, mapValue) ->
                put(key.toString(), canonicalize(mapValue))
            }
        }
        is List<*> -> value.map(::canonicalize)
        else -> value
    }
}
