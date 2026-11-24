/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.modelview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ValueSnapshotTest {
    @Test
    fun `numbers keep how the simulation writes them, and other values become text`() {
        assertEquals(ValueSnapshot.Numeric(3.0, "3"), ValueSnapshot.of(3))
        assertEquals(ValueSnapshot.Flag(true), ValueSnapshot.of(true))
        assertEquals(ValueSnapshot.Text("null"), ValueSnapshot.of(null))
    }

    @Test
    fun `collections and maps keep their structure, sets included`() {
        val value = assertIs<ValueSnapshot.Items>(ValueSnapshot.of(listOf(setOf("a"), mapOf(1 to true))))
        assertEquals(ValueSnapshot.Items(listOf(ValueSnapshot.Text("a")), unique = true), value.items[0])
        val entry = EntrySnapshot(ValueSnapshot.Numeric(1.0, "1"), ValueSnapshot.Flag(true))
        assertEquals(ValueSnapshot.Entries(listOf(entry)), value.items[1])
        assertEquals("[[a], {1=true}]", value.text)
    }

    @Test
    fun `iterables that are not collections, and arrays, are items too`() {
        val tuple = Iterable { listOf(1, 2, 3).iterator() }
        assertEquals(3, assertIs<ValueSnapshot.Items>(ValueSnapshot.of(tuple)).size)
        assertEquals("[1, 2]", ValueSnapshot.of(intArrayOf(1, 2)).text)
    }

    @Test
    fun `large collections keep their first items and their size`() {
        val value = assertIs<ValueSnapshot.Items>(ValueSnapshot.of((1..5).toList(), ValueLimits(items = 2)))
        assertEquals(listOf("1", "2"), value.items.map { it.text })
        assertEquals(5, value.size)
        assertEquals("[1, 2, …]", value.text)
    }

    @Test
    fun `collections beyond the depth limit keep only their size`() {
        val value = assertIs<ValueSnapshot.Items>(ValueSnapshot.of(listOf(listOf(1, 2)), ValueLimits(depth = 1)))
        assertEquals(ValueSnapshot.Items(emptyList(), size = 2), value.items.single())
        assertEquals("[[…]]", value.text)
    }

    @Test
    fun `long texts are cut`() {
        assertEquals("abc…", ValueSnapshot.of("abcdef", ValueLimits(textLength = 3)).text)
    }
}
