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
    fun `observers are the registrants of the set`() {
        set.onChange(this, invokeOnRegistration = false) { }
        assertEquals(listOf<Any>(this), set.observers)
    }

    @Test
    fun `an advertised observer can be stopped`() {
        val seen = mutableListOf<Set<Int>>()
        set.onChange(this, invokeOnRegistration = false) { seen += it }
        set.observers.forEach(set::stopWatching)
        set.add(2)
        assertEquals(emptyList(), seen)
        assertEquals(emptyList(), set.observers)
    }
}
