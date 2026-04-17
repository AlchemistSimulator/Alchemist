/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.ViewportEdge
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class AlchemistNodeAdapterTest {
    @Test
    fun `canonical edge sorts endpoints`() {
        assertEquals(ViewportEdge(2, 5), canonicalEdge(5, 2))
    }

    @Test
    fun `canonical edge ignores self loops`() {
        assertNull(canonicalEdge(4, 4))
    }

    @Test
    fun `canonical edge deduplicates mutual links`() {
        val uniqueEdges = setOfNotNull(canonicalEdge(1, 3), canonicalEdge(3, 1))
        assertEquals(1, uniqueEdges.size)
        assertTrue(uniqueEdges.contains(ViewportEdge(1, 3)))
    }
}
