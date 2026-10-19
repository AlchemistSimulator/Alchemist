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
import arrow.core.none
import arrow.core.some
import it.unibo.alchemist.model.observables.ObservableMutableMap
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservableMutableMapKeyObservablesTest {
    private val map = ObservableMutableMap(mapOf("initial" to 1))

    private fun stopObservingTheMap() = map.onChange { }.dispose()

    @Test
    fun `stopping a map observer keeps the values of initial keys observable`() {
        stopObservingTheMap()
        assertEquals(1.some(), map["initial"].current)
    }

    @Test
    fun `stopping a map observer keeps the values of added keys observable`() {
        map.put("added", 2)
        stopObservingTheMap()
        assertEquals(2.some(), map["added"].current)
    }

    @Test
    fun `stopping a map observer keeps removed keys absent`() {
        map["initial"].onChange { }.dispose()
        map.remove("initial")
        stopObservingTheMap()
        assertEquals(none(), map["initial"].current)
    }

    @Test
    fun `a handle taken for an absent key follows later insertions`() {
        val handle = map["late"]
        stopObservingTheMap()
        map.put("late", 3)
        assertEquals(3.some(), handle.current)
        val seen = mutableListOf<Option<Int>>()
        handle.onChange(notifyOnRegistration = false) { seen += it }
        map.remove("late")
        map.put("late", 4)
        assertEquals(listOf(none(), 4.some()), seen)
    }

    @Test
    fun `a key subscription can be released`() {
        val seen = mutableListOf<Option<Int>>()
        map["initial"].onChange(notifyOnRegistration = false) { seen += it }.dispose()
        map.put("initial", 2)
        assertEquals(emptyList(), seen)
    }

    @Test
    fun `releasing a map subscription keeps the key subscriptions`() {
        val seen = mutableListOf<Option<Int>>()
        val subscription = map.onChange(notifyOnRegistration = false) { }
        map["initial"].onChange(notifyOnRegistration = false) { seen += it }
        subscription.dispose()
        map.put("initial", 2)
        assertEquals(listOf(2.some()), seen)
    }

    @Test
    fun `key observers of a replaced map read the whole replacement`() {
        val seen = mutableListOf<Map<String, Int>>()
        map["initial"].onChange(notifyOnRegistration = false) { seen += map.current }
        map.clearAndPutAll(mapOf("replacement" to 2))
        assertEquals(listOf(mapOf("replacement" to 2)), seen)
    }

    @Test
    fun `map observers receive each map once when a key observer changes the map on a put`() {
        map["initial"].onChange(notifyOnRegistration = false) { if (it == 2.some()) map.put("other", 3) }
        val seen = mutableListOf<Map<String, Int>>()
        map.onChange(notifyOnRegistration = false) { seen += it }
        map.put("initial", 2)
        assertEquals(listOf(mapOf("initial" to 2, "other" to 3)), seen)
    }

    @Test
    fun `map observers receive each map once when a key observer changes the map on a remove`() {
        map["initial"].onChange(notifyOnRegistration = false) { if (it == none<Int>()) map.put("other", 3) }
        val seen = mutableListOf<Map<String, Int>>()
        map.onChange(notifyOnRegistration = false) { seen += it }
        map.remove("initial")
        assertEquals(listOf(mapOf("other" to 3)), seen)
    }
}
