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
 * For [Iterable], the elements are hashed, meaning that
 * for example a [List] and a [Set] containing the same elements should return the same hash.
 *
 * @param data the data to hash
 */
fun hashMurmur3_32(vararg data: Any): Int = hash(MURMUR3_32.newHasher(), data)

private fun hash(hasher: Hasher, vararg data: Any): Int =
    data.forEach { hasher.put(it) }.run { hasher.hash().asInt() }

private fun Hasher.put(item: Any): Unit {
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
            is Iterable<*> -> item.forEach { put(it!!) }
            else -> putInt(item.hashCode())
        }
    }