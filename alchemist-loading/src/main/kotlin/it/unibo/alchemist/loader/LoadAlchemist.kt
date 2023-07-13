/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader

import it.unibo.alchemist.loader.m2m.SimulationModel
import it.unibo.alchemist.util.ClassPathScanner
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.net.URL

/**
 * Loads Alchemist simulations from a variety of resources.
 */
object LoadAlchemist {

    /**
     * Load from an [input] [String].
     */
    @JvmStatic
    fun from(input: String, model: AlchemistModelProvider, overrides: List<String> = emptyList()) =
        SimulationModel.fromMap(
            applyOverrides(model.from(input), overrides),
        )

    /**
     * Load from a [reader].
     */
    @JvmStatic
    fun from(reader: Reader, model: AlchemistModelProvider, overrides: List<String> = emptyList()) =
        SimulationModel.fromMap(
            applyOverrides(model.from(reader), overrides),
        )

    /**
     * Load from an [InputStream].
     */
    @JvmStatic
    fun from(stream: InputStream, model: AlchemistModelProvider, overrides: List<String> = emptyList()) =
        SimulationModel.fromMap(
            applyOverrides(model.from(stream), overrides),
        )

    /**
     * Load from an [url].
     */
    @JvmStatic
    fun from(url: URL, model: AlchemistModelProvider, overrides: List<String> = emptyList()) =
        SimulationModel.fromMap(applyOverrides(model.from(url), overrides))

    /**
     * Load from an [url].
     */
    @JvmStatic
    fun from(url: URL, overrides: List<String> = emptyList()) =
        from(url, modelForExtension(url.path.takeLastWhile { it != '.' }), overrides)

    /**
     * Load from a [file].
     */
    @JvmStatic
    fun from(file: File) = from(file.inputStream(), modelForExtension(file.extension))

    /**
     * Load from a [string].
     */
    @JvmStatic
    fun from(string: String) = from(File(string))

    @JvmStatic
    private fun modelForExtension(extension: String) = ClassPathScanner
        .subTypesOf<AlchemistModelProvider>("it.unibo.alchemist.loader.providers")
        .mapNotNull { it.kotlin.objectInstance }
        .filter { it.fileExtensions.matches(extension) }
        .also { require(it.size == 1) { "None or conflicting loaders for extension $extension: $it" } }
        .first()

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    private fun applyOverrides(map: Map<String, *>, overrides: List<String>): Map<String, *> {
        return if (overrides.isEmpty()) {
            map
        } else {
            val newMap = LinkedHashMap(map)
            overrides.forEach { override ->
                val key = override.substringBefore("=")
                val value = override.substringAfter("=")
                val keyChain = key.split(".")
                val accessors = keyChain.subList(0, keyChain.size - 1)
                val target = keyChain[keyChain.size - 1]

                var pointer: MutableMap<*, *> = newMap
                accessors.forEach { accessor ->
                    val maybeArrayAccessor = parseArrayAccessor(accessor)
                    pointer = if (maybeArrayAccessor != null) {
                        if (pointer[maybeArrayAccessor.key] == null) {
                            throw IllegalArgumentException("key $accessor in $key does not exist in simulation variables")
                        }
                        val x = pointer[maybeArrayAccessor.key]
                        val list = x as List<MutableMap<*, *>>
                        list[maybeArrayAccessor.index]
                    } else {
                        if (pointer[accessor] == null) {
                            throw IllegalArgumentException("key $accessor in $key does not exist in simulation variables")
                        }
                        pointer[accessor] as MutableMap<*, *>
                    }
                }

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
                        val castList = pointer[target] as List<*>
                        when (castList[0]) {
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
                        }
                    }

                    null -> {
                        throw UnsupportedOperationException("Cannot ovveride null value $key")
                    }

                    else -> throw UnsupportedOperationException("Cannot ovveride type ${pointer[target]?.javaClass?.name}")
                }
            }

            newMap
        }
    }

    private data class AccessorParseResult(
        val key: String,
        val index: Int,
    )

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
}
