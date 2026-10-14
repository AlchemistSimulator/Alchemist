/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.observation

import arrow.core.Option
import arrow.core.some
import it.unibo.alchemist.model.observables.ObservableMutableList
import it.unibo.alchemist.model.observables.ObservableMutableMap
import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import kotlin.test.Test
import kotlin.test.assertEquals

class ReentrantNotificationTest {
    @Test
    fun `a derived observable caches the newest value when a callback changes its source`() {
        val source = observe(0)
        val mapped = source.map { it }
        source.onChange(FIRST, false) { if (it == 1) source.current = 2 }
        val received = mutableListOf<Int>()
        mapped.onChange(SECOND, false) { received += it }
        source.current = 1
        assertEquals(2, mapped.current)
        assertEquals(2, received.last())
    }

    @Test
    fun `later list observers last receive the newest snapshot`() {
        val list = ObservableMutableList<Int>()
        list.onChange(FIRST, false) { if (it == listOf(1)) list.add(2) }
        val received = mutableListOf<List<Int>>()
        list.onChange(SECOND, false) { received += it }
        list.add(1)
        assertEquals(listOf(1, 2), received.last())
    }

    @Test
    fun `later map key observers last receive the newest value`() {
        val map = ObservableMutableMap<String, Int>()
        map[KEY].onChange(FIRST, false) { if (it == 1.some()) map[KEY] = 2 }
        val received = mutableListOf<Option<Int>>()
        map[KEY].onChange(SECOND, false) { received += it }
        map[KEY] = 1
        assertEquals(2.some(), received.last())
    }

    @Test
    fun `later map observers last receive the newest snapshot`() {
        val map = ObservableMutableMap<String, Int>()
        map.onChange(FIRST, false) { if (it == mapOf(KEY to 1)) map[KEY] = 2 }
        val received = mutableListOf<Map<String, Int>>()
        map.onChange(SECOND, false) { received += it }
        map[KEY] = 1
        assertEquals(mapOf(KEY to 2), received.last())
    }

    @Test
    fun `later observers last receive the newest value when a callback changes the source`() {
        val source = observe(0)
        source.onChange(FIRST, false) { if (it == 1) source.current = 2 }
        val received = mutableListOf<Int>()
        source.onChange(SECOND, false) { received += it }
        source.current = 1
        assertEquals(2, received.last())
    }

    private companion object {
        private const val FIRST = "first"
        private const val KEY = "key"
        private const val SECOND = "second"
    }
}
