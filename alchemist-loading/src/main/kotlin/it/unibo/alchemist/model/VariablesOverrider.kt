/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader

/**
 * Use this to override a map of variables with a list of resolvable key-value pairs.
 */
object VariablesOverrider {

    /**
     * Overrides a map of variables with a list of resolvable key-value pairs.
     *
     * For example, given the following yml file parsed to a map of maps (and lists):
     *
     * _test:
     *   str: replaceme
     *   strL: [ replaceme, replaceme ]
     *   arr:
     *     - nst1-1: replaceme
     *       nst1-2: replaceme
     *     - nst2-1:
     *         - nst2-1-1: replaceme
     *
     * In order to override each variable we should provide:
     *
     * [
     *  "_test.str=test"
     *  "_test.strL=[test1, test2]",
     *  "_test.arr[0].nst1-1=test",
     *  "_test.arr[0].nst1-2=test",
     *  "_test.arr[1].nst2-1[0].nst2-1-1=test",
     * ]
     *
     * The overrider supports common variable types (such as string, int, double...).
     * The overrider cannot create new variables.
     * The overrider ignores configuration variables prefixed by __.
     */
    @JvmStatic
    fun applyOverrides(map: Map<String, *>, overrides: List<String>): Map<String, *> {
        return if (overrides.isEmpty()) {
            map
        } else {
            val newMap = LinkedHashMap(map)
            overrides.filter { !it.startsWith("__") }.forEach { applyOverride(it, newMap) }
            newMap
        }
    }

    @JvmStatic
    private fun applyOverride(override: String, newMap: MutableMap<String, Any?>) {
        val key = override.substringBefore("=")
        val value = override.substringAfter("=")
        val keyChain = key.split(".")
        val accessors = keyChain.subList(0, keyChain.size - 1)
        val target = keyChain[keyChain.size - 1]
        val pointer = navigateAccessors(newMap, accessors, key)
        overrideValue(pointer, target, value, key)
    }

    @JvmStatic
    private fun navigateAccessors(
        newMap: MutableMap<String, Any?>,
        accessors: List<String>,
        key: String,
    ): MutableMap<*, *> {
        var pointer: MutableMap<*, *> = newMap
        accessors.forEach { accessor ->
            val maybeArrayAccessor = parseArrayAccessor(accessor)
            pointer = if (maybeArrayAccessor != null) {
                navigateListAccessor(pointer, maybeArrayAccessor, key)
            } else {
                navigateMapAccessor(pointer, accessor, key)
            }
        }
        return pointer
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    private fun navigateListAccessor(
        pointer: MutableMap<*, *>,
        arrayAccessor: AccessorParseResult,
        key: String,
    ): MutableMap<*, *> {
        if (pointer[arrayAccessor.key] == null) {
            throw IllegalArgumentException("key ${arrayAccessor.key} in $key does not exist in simulation variables")
        }
        val listPointer = pointer[arrayAccessor.key]
        val list = listPointer as List<MutableMap<*, *>>
        return list[arrayAccessor.index]
    }

    @JvmStatic
    private fun navigateMapAccessor(pointer: MutableMap<*, *>, accessor: String, key: String): MutableMap<*, *> {
        if (pointer[accessor] == null) {
            throw IllegalArgumentException("key $accessor in $key does not exist in simulation variables")
        }
        return pointer[accessor] as MutableMap<*, *>
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    private fun overrideValue(pointer: MutableMap<*, *>, target: String, value: String, key: String) {
        when (pointer[target]) {
            is Int -> {
                val castPointer = pointer as MutableMap<String, Int>
                castPointer[target] = value.toInt()
            }

            is String -> {
                val castPointer = pointer as MutableMap<String, String>
                castPointer[target] = value
            }

            is Double -> {
                val castPointer = pointer as MutableMap<String, Double>
                castPointer[target] = value.toDouble()
            }

            is List<*> -> {
                castListPointer(pointer, target, value)
            }

            null -> {
                throw UnsupportedOperationException("Cannot ovveride null value $key")
            }

            else -> throw UnsupportedOperationException("Cannot ovveride type ${pointer[target]?.javaClass?.name}")
        }
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    private fun castListPointer(pointer: MutableMap<*, *>, target: String, value: String) {
        val castList = pointer[target] as List<*>
        return when (castList[0]) {
            is Int -> {
                val castPointer = pointer as MutableMap<String, List<Int>>
                castPointer[target] = parseList(value) { it.toInt() }
            }

            is String -> {
                val castPointer = pointer as MutableMap<String, List<String>>
                castPointer[target] = parseList(value) { it }
            }

            is Double -> {
                val castPointer = pointer as MutableMap<String, List<Double>>
                castPointer[target] = parseList(value) { it.toDouble() }
            }

            else -> {
                throw UnsupportedOperationException("Cannot ovveride type ${pointer[target]?.javaClass?.name}")
            }
        }
    }

    @JvmStatic
    private fun parseArrayAccessor(accessor: String): AccessorParseResult? {
        val regex = "\\[([0-9]+)\\]".toRegex()
        val key = accessor.substringBefore("[")
        val value = regex.find(accessor)?.groups?.get(1)?.value?.toInt()
        return if (value != null) {
            AccessorParseResult(key, value)
        } else {
            null
        }
    }

    @JvmStatic
    private fun <A> parseList(value: String, mapper: (String) -> A): List<A> {
        return value.substringAfter('[')
            .substringBefore(']')
            .replace(" ", "")
            .split(",")
            .map { mapper(it) }
    }

    private data class AccessorParseResult(
        val key: String,
        val index: Int,
    )
}
