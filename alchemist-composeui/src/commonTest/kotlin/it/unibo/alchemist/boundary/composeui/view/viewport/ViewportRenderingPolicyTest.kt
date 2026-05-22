/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.viewport

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import it.unibo.alchemist.boundary.composeui.model.LinkRenderMode
import it.unibo.alchemist.boundary.composeui.model.ViewportEdge
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class ViewportRenderingPolicyTest {
    @Test
    fun `frame culls off-screen nodes and edges`() {
        val scene = ViewportScene(
            nodes = listOf(
                ViewportNode(id = 1, coordinates = persistentListOf(0.0, 0.0)),
                ViewportNode(id = 2, coordinates = persistentListOf(1.0, 1.0)),
                ViewportNode(id = 3, coordinates = persistentListOf(2.0, 2.0)),
            ).toImmutableList(),
            showLinks = true,
        )
        val cache = ViewportSceneCache(
            scene = scene,
            baseCenters = listOf(
                Offset(50f, 50f),
                Offset(400f, 400f),
                Offset(80f, 80f),
            ).toImmutableList(),
            indexedEdges = listOf(
                IndexedEdge(fromIndex = 0, toIndex = 2),
                IndexedEdge(fromIndex = 0, toIndex = 1),
            ).toImmutableList(),
        )

        val frame = buildViewportFrame(cache, IntSize(120, 120), ViewportCameraState())

        assertEquals(listOf(0, 2), frame.visibleNodeIndices)
        assertEquals(listOf(IndexedEdge(fromIndex = 0, toIndex = 2)), frame.visibleEdges)
    }

    @Test
    fun `sampled mode caps the number of visible edges per frame`() {
        val scene = ViewportScene(
            nodes = listOf(
                ViewportNode(id = 1, coordinates = persistentListOf(0.0, 0.0)),
                ViewportNode(id = 2, coordinates = persistentListOf(1.0, 1.0)),
            ).toImmutableList(),
            edges = List(MaxDrawnEdgesPerFrame + 12) { ViewportEdge(1, 2) }.toImmutableList(),
            showLinks = true,
            linkRenderMode = LinkRenderMode.SAMPLED,
        )
        val cache = ViewportSceneCache(
            scene = scene,
            baseCenters = persistentListOf(Offset(20f, 20f), Offset(80f, 80f)),
            indexedEdges = List(MaxDrawnEdgesPerFrame + 12) { IndexedEdge(fromIndex = 0, toIndex = 1) }
                .toImmutableList(),
        )

        val frame = buildViewportFrame(cache, IntSize(120, 120), ViewportCameraState())

        assertEquals(MaxDrawnEdgesPerFrame, frame.visibleEdges.size)
    }
}
