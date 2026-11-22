/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.model.ViewportEdge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EdgeKeyTest {
    @Test
    fun `edge keys sort the endpoints`() {
        assertEquals(ViewportEdge(2, 5), EdgeKey.of(5, 2)?.edge)
    }

    @Test
    fun `edge keys ignore self links`() {
        assertNull(EdgeKey.of(4, 4))
    }

    @Test
    fun `edge keys merge mutual links`() {
        assertEquals(setOf(ViewportEdge(1, 3)), setOfNotNull(EdgeKey.of(1, 3)?.edge, EdgeKey.of(3, 1)?.edge))
    }
}
