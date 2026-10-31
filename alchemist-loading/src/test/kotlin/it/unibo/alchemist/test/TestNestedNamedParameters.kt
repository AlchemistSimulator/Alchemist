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

class TestNestedNamedParameters {
    @Test
    fun `objects passed as named parameters are built for the parameter with their name`() {
        val environment = loadAlchemistFromResource("regression/nested-named-parameter-with-omitted-optionals.yml")
            .getDefault<Any, Euclidean2DPosition>()
            .environment
        val layer = environment.layers.values.single()
        // level (3) times scale (2), plus the default offset (0)
        assertEquals(6.0, layer.currentValueAt(Euclidean2DPosition(0.0, 0.0)))
    }
}
