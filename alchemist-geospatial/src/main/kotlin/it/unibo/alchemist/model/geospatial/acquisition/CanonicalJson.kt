/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.acquisition

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.util.TreeMap

/**
 * Deterministic JSON encoding used to derive stable cache keys and submit bodies.
 *
 * "Canonical" here means exactly one normalization: **map keys are sorted recursively** at every
 * nesting level, so a request encodes identically regardless of the key order it was authored in.
 * **List order is preserved**, since in CDS/EWDS it is semantic (e.g. `area: [N, W, S, E]`
 * has a different meaning than `[W, N, E, S]`).
 */
internal object CanonicalJson {

    private val gson: Gson = GsonBuilder().serializeNulls().create()

    /**
     * Encodes [value] as canonical JSON.
     *
     * @param value a serializable value, typically a nested [Map]/[List].
     * @return its canonical JSON representation: map keys sorted recursively, list order untouched.
     */
    fun encode(value: Any): String = gson.toJson(canonicalize(value))

    /**
     * Recursively rewrites [value] into a form Gson serializes deterministically: every [Map]
     * becomes a key-sorted [TreeMap], every [List] keeps its order,
     * scalars (strings, booleans, numbers, nulls) are left untouched.
     */
    private fun canonicalize(value: Any?): Any? = when (value) {
        // maintains the keys sorted
        is Map<*, *> -> TreeMap<String, Any?>().apply {
            value.forEach { (key, mapValue) ->
                // recursively evaluates nested structures
                put(key.toString(), canonicalize(mapValue))
            }
        }
        is List<*> -> value.map(::canonicalize)
        else -> value
    }
}
