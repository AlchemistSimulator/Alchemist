/*
 * Copyright (C) 2010-2019, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.kotlin

import com.google.common.hash.HashFunction
import com.google.common.hash.Hashing
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class TestHashes {

    @Test
    fun testMurmur3_32(): Unit = testAlgorithm({ e -> hashMurmur332(e) }, Hashing.murmur3_32())

    private fun testAlgorithm(hash: (Any) -> Int, hashFunction: HashFunction) {
        testSingleElements(hash, hashFunction)
        testMultipleElements(hash, hashFunction)
        testOrder(hash)
    }

    private fun testSingleElements(hash: (Any) -> Int, hashFunction: HashFunction) {
        assertEquals(hash(5L), hashFunction.newHasher().putLong(5L).hash().asInt())
        assertEquals(hash(5), hashFunction.newHasher().putInt(5).hash().asInt())
        assertEquals(hash(5.toShort()), hashFunction.newHasher().putShort(5).hash().asInt())
        assertEquals(hash(5.toByte()), hashFunction.newHasher().putByte(5).hash().asInt())
        assertEquals(hash(5.0), hashFunction.newHasher().putDouble(5.0).hash().asInt())
        assertEquals(hash(5.0f), hashFunction.newHasher().putFloat(5.0f).hash().asInt())
        assertEquals(hash('5'), hashFunction.newHasher().putChar('5').hash().asInt())
        assertEquals(hash(false), hashFunction.newHasher().putBoolean(false).hash().asInt())
        assertEquals(hash("5"), hashFunction.newHasher().putString("5", Charsets.UTF_16).hash().asInt())
    }

    private fun testMultipleElements(hash: (Any) -> Int, hashFunction: HashFunction) {
        val sequence = sequenceOf("5", '5', 5, true)
        val list = listOf("5", '5', 5, true)
        val expected = hashFunction.newHasher()
            .putString("5", Charsets.UTF_16)
            .putChar('5')
            .putInt(5)
            .putBoolean(true)
            .hash().asInt()
        assertEquals(hash(sequence), expected)
        assertEquals(hash(list), expected)
        assertEquals(hash(list), hash(list.toMutableList()))
    }

    private fun testOrder(hash: (Any) -> Int) {
        sequenceOf("5", '5', 5, true).let { sequence ->
            assertNotEquals(hash(sequence.sortedByDescending { it.hashCode() }), hash(sequence.sortedBy { it.hashCode() }))
        }
    }
}
