/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.util

import it.unibo.alchemist.boundary.modelproviders.YamlProvider

/**
 * Use this to override a map of variables with a list of resolvable key-value pairs.
 */
object VariablesOverrider {

    /**
     * Apply overrides to a map of variables.
     * @parameter map Map to be overriden
     * @parameter overrides list of valid String yaml strings containing overrides
     */
    @JvmStatic
    fun applyOverrides(map: Map<String, *>, overrides: List<String>): Map<String, *> {
        return if (overrides.isEmpty()) {
            map
        } else {
            val newMap = map.toMutableMap()
            overrides.forEach { applyOverride(it, newMap) }
            newMap
        }
    }

    @JvmStatic
    private fun applyOverride(override: String, newMap: MutableMap<String, Any?>) {
        val overrideMap = YamlProvider.from(override)
        overrideMap.forEach { entry ->
            mergeInto(entry.key, entry.value, newMap)
        }
    }

    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    private fun mergeInto(key: String, value: Any?, newMap: MutableMap<String, Any?>) {
        when (value) {
            is MutableMap<*, *> -> {
                if (newMap[key] is MutableMap<*, *>) {
                    val castValue = value as MutableMap<String, Any?>
                    val pointer = newMap[key] as MutableMap<String, Any?>
                    castValue.forEach { entry ->
                        mergeInto(entry.key, entry.value, pointer)
                    }
                } else {
                    newMap[key] = value
                }
            }

            else -> newMap[key] = value
        }
    }
}
