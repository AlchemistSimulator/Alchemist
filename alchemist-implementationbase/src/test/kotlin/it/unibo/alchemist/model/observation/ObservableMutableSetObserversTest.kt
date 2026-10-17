/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.observation

import it.unibo.alchemist.model.observables.ObservableMutableSet
import kotlin.test.Test
import kotlin.test.assertEquals

class ObservableMutableSetObserversTest {
    private val set = ObservableMutableSet(1)

    @Test
    fun `an advertised observer can be stopped`() {
        val seen = mutableListOf<Set<Int>>()
        set.onChange(this, invokeOnRegistration = false) { seen += it }
        set.observers.forEach(set::stopWatching)
        set.add(2)
        assertEquals(emptyList(), seen)
        assertEquals(emptyList(), set.observers)
    }

    @Test
    fun `observers are the registrants of the set`() {
        set.onChange(this, invokeOnRegistration = false) { }
        assertEquals(listOf<Any>(this), set.observers)
    }

    @Test
    fun `replacing the contents notifies the observers of each changed item`() {
        val membership = listOf(1, 2, 3).associateWith { item ->
            mutableListOf<Boolean>().also { seen ->
                set.containsItem(item).subscribe(invokeOnSubscription = false) {
                    seen +=
                        it
                }
            }
        }
        set.clearAndAddAll(setOf(2, 3))
        assertEquals(mapOf(1 to listOf(false), 2 to listOf(true), 3 to listOf(true)), membership)
    }

    @Test
    fun `replacing the contents notifies the observers of the set once`() {
        val seen = mutableListOf<Set<Int>>()
        set.onChange(this, invokeOnRegistration = false) { seen += it }
        set.clearAndAddAll(setOf(2, 3))
        assertEquals(listOf(setOf(2, 3)), seen)
    }

    @Test
    fun `replacing the contents with the same items notifies nobody`() {
        val seen = mutableListOf<Set<Int>>()
        set.onChange(this, invokeOnRegistration = false) { seen += it }
        set.clearAndAddAll(setOf(1))
        assertEquals(emptyList(), seen)
    }
}
