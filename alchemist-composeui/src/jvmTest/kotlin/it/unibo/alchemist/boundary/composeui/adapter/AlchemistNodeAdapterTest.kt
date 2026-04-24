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
import it.unibo.alchemist.boundary.composeui.model.LinkRenderMode
import it.unibo.alchemist.boundary.composeui.view.viewport.FullEdgeRenderLimit
import it.unibo.alchemist.boundary.composeui.view.viewport.MaxDrawnEdgesPerFrame
import it.unibo.alchemist.boundary.composeui.view.viewport.SampledEdgeRenderLimit
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

    @Test
    fun `edge snapshot samples medium-sized graphs`() {
        val snapshot = collectEdgeSnapshot(
            edgePairs = (1..(FullEdgeRenderLimit + 50)).asSequence().map { edgeId ->
                edgeId to (edgeId + 1)
            },
        )

        assertEquals(LinkRenderMode.SAMPLED, snapshot.renderMode)
        assertEquals(FullEdgeRenderLimit + 50, snapshot.edgeCount)
        assertEquals(MaxDrawnEdgesPerFrame, snapshot.edges.size)
    }

    @Test
    fun `edge snapshot hides very large graphs`() {
        val snapshot = collectEdgeSnapshot(
            edgePairs = (1..(SampledEdgeRenderLimit + 1)).asSequence().map { edgeId ->
                edgeId to (edgeId + 1)
            },
        )

        assertEquals(LinkRenderMode.HIDDEN, snapshot.renderMode)
        assertTrue(snapshot.edges.isEmpty())
        assertTrue(snapshot.edgeCount > SampledEdgeRenderLimit)
    }
}
