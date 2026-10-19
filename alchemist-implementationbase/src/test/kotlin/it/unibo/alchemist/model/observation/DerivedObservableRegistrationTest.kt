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

class DerivedObservableRegistrationTest {
    private val source = observe(0)
    private val derived = source.map { it * 10 }

    @Test
    fun `a source change made by the initial callback reaches the derived observable`() {
        val received = mutableListOf<Int>()
        derived.onChange { value ->
            received += value
            if (value == 0) {
                source.current = 1
            }
        }
        assertEquals(10, derived.current)
        assertEquals(listOf(0, 10), received)
    }
}
