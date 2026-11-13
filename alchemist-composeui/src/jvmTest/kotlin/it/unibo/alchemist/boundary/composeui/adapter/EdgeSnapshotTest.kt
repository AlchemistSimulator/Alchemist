/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.model.LinkRenderMode
import it.unibo.alchemist.boundary.composeui.view.viewport.FULL_EDGE_RENDER_LIMIT
import it.unibo.alchemist.boundary.composeui.view.viewport.MAX_DRAWN_EDGES_PER_FRAME
import it.unibo.alchemist.boundary.composeui.view.viewport.SAMPLED_EDGE_RENDER_LIMIT
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EdgeSnapshotTest {
    @Test
    fun `edge snapshot samples medium-sized graphs`() {
        val snapshot = EdgeSnapshot.of(chain(FULL_EDGE_RENDER_LIMIT + 50))
        assertEquals(LinkRenderMode.SAMPLED, snapshot.renderMode)
        assertEquals(FULL_EDGE_RENDER_LIMIT + 50, snapshot.edgeCount)
        assertEquals(MAX_DRAWN_EDGES_PER_FRAME, snapshot.edges.size)
    }

    @Test
    fun `edge snapshot hides very large graphs`() {
        val snapshot = EdgeSnapshot.of(chain(SAMPLED_EDGE_RENDER_LIMIT + 1))
        assertEquals(LinkRenderMode.HIDDEN, snapshot.renderMode)
        assertTrue(snapshot.edges.isEmpty())
        assertTrue(snapshot.edgeCount > SAMPLED_EDGE_RENDER_LIMIT)
    }

    private companion object {
        /** [length] links, each node to the next. */
        fun chain(length: Int): Sequence<Pair<Int, Int>> = (1..length).asSequence().map { it to it + 1 }
    }
}
