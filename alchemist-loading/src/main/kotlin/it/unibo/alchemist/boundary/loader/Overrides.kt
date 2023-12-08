/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.loader

import it.unibo.alchemist.boundary.modelproviders.YamlProvider

/**
 * Use this to override a map of variables with a list of resolvable key-value pairs.
 */
internal object Overrides {

    /**
     * Apply overrides to a map of variables.
     * @parameter map Map to be overriden
     * @parameter overrides list of valid String yaml strings containing overrides
     */
    @JvmStatic
    fun Map<String, *>.overrideAll(overrides: List<String>): Map<String, *> = when {
        overrides.isEmpty() -> this
        else ->
            this.toMutableMap().also { mutableMap ->
                overrides.forEach { applyOverride(it, mutableMap) }
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
                if (
                    value.isNotEmpty() &&
                    newMap[key] is MutableMap<*, *>
                ) {
                    val currentTreeNode = newMap[key] as MutableMap<*, *>
                    value.forEach { entry ->
                        if (
                            entry.key is String &&
                            currentTreeNode.isNotEmpty() &&
                            currentTreeNode.keys.toList()[0] is String
                        ) {
                            mergeInto(entry.key as String, entry.value, currentTreeNode as MutableMap<String, Any?>)
                        }
                    }
                } else {
                    newMap[key] = value
                }
            }

            else -> newMap[key] = value
        }
    }
}
