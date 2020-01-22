/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.kotlin

import com.google.common.hash.Hasher
import com.google.common.hash.Hashing

private val MURMUR3_32 = Hashing.murmur3_32()

/**
 * Hashes a number of [Any]s with [Hashing.murmur3_32].
 * The charset used for strings is [Charsets.UTF_16].
 * For [Iterable] and [Sequence], the elements are hashed rather than the [Iterable] or [Sequence] itself.
 * If the [Iterable] or [Sequence] contains a null element, it is skipped.
 *
 * @param data the data to hash
 */
fun <T: Any> hashMurmur3_32(vararg data: T): Int = hash(MURMUR3_32.newHasher(), *data)

private fun <T: Any> hash(hasher: Hasher, vararg data: T): Int =
    data.forEach { hasher.put(it) }.run { hasher.hash().asInt() }

private fun <T: Any> Hasher.put(item: T): Unit {
    when (item) {
        is Long -> putLong(item)
        is Int -> putInt(item)
        is Short -> putShort(item)
        is Byte -> putByte(item)
        is Double -> putDouble(item)
        is Float -> putFloat(item)
        is Char -> putChar(item)
        is Boolean -> putBoolean(item)
        is String -> putString(item, Charsets.UTF_16)
        is Iterable<*> -> item.forEach { if (it != null) put(it) }
        is Sequence<*> -> item.forEach { if (it != null) put(it) }
        is Map<*, *> -> {
            item.entries.forEach { (key, value) ->
                if (value != null) {
                    if (key != null) {
                        put(key)
                    }
                    put(value)
                }
            }
        }
        else -> putInt(item.hashCode())
    }
}