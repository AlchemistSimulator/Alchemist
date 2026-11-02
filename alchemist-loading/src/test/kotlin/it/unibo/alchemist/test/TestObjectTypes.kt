/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.test.AlchemistTesting.loadAlchemistFromResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class TestObjectTypes {
    @Test
    fun `objects can be used as types, both as parameters and as entities of the simulation`() {
        val environment = loadAlchemistFromResource("regression/object-as-parameter.yml")
            .getDefault<Any, Euclidean2DPosition>()
            .environment
        val values = environment.layers.values.map { it.currentValueAt(Euclidean2DPosition(0.0, 0.0)) }.toSet()
        // the shifted layer: level (3) plus the amount of UnitShift (1); the zero layer: 0
        assertEquals(setOf(4.0, 0.0), values)
    }

    @Test
    fun `objects given parameters fail explaining that they take none`() {
        val failure = assertFails {
            loadAlchemistFromResource("regression/object-with-parameters.yml")
                .getDefault<Any, Euclidean2DPosition>()
        }
        val messages = generateSequence(failure) { it.cause }.mapNotNull { it.message }.toList()
        assertTrue(
            messages.any { "UnitShift is an object, so it takes no parameters" in it },
            "Unexpected failure messages: $messages",
        )
    }
}
