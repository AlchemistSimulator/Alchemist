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
import it.unibo.alchemist.model.observables.util.Observables.switchMap
import kotlin.test.Test
import kotlin.test.assertEquals

class SwitchMapTest {
    private val source = observe(0)
    private val selected = mutableListOf<MutableObservable<Int>>()

    // Every selection builds a fresh inner observable, holding the number of selections made so far.
    private val switched = source.switchMap { observe(selected.size).also(selected::add) }

    @Test
    fun `subscribing selects the inner observable once and follows it`() {
        val emissions = mutableListOf<Int>()
        switched.subscribe { emissions += it }
        assertEquals(listOf(0), emissions)
        assertEquals(1, selected.size)
        selected.single().current = 42
        assertEquals(listOf(0, 42), emissions)
    }

    @Test
    fun `a source change switches to a newly selected inner observable`() {
        val emissions = mutableListOf<Int>()
        switched.subscribe { emissions += it }
        source.current = 1
        assertEquals(listOf(0, 1), emissions)
        selected.first().current = 42
        assertEquals(listOf(0, 1), emissions)
    }
}
