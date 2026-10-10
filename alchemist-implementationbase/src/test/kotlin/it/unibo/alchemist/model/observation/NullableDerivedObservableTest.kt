/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.model.observation

import it.unibo.alchemist.model.observables.util.MutableObservables.observe
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NullableDerivedObservableTest {
    private val source = observe(0)
    private var computations = 0
    private val derived: Observable<String?> = source.map {
        computations++
        null
    }

    @Test
    fun `a derived observable does not repeat null emissions`() {
        val emissions = mutableListOf<String?>()
        derived.subscribe { emissions += it }
        source.current = 1
        source.current = 2
        assertEquals(listOf<String?>(null), emissions)
    }

    @Test
    fun `a listening derived observable reads a cached null without recomputing it`() {
        derived.subscribe { }
        val computationsAfterSubscription = computations
        assertNull(derived.current)
        assertEquals(computationsAfterSubscription, computations)
    }
}
