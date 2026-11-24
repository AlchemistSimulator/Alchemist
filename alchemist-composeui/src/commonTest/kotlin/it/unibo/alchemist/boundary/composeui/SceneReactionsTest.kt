/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.model.InspectorState.NodeGroup
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.collections.immutable.persistentListOf

class SceneReactionsTest {
    private val scene = ViewportScene(
        nodes = persistentListOf(
            ViewportNode(id = 1, coordinates = persistentListOf(0.0, 0.0), reactions = persistentListOf("fresh")),
            ViewportNode(id = 2, coordinates = persistentListOf(1.0, 1.0)),
        ),
    )

    @Test
    fun `only the requested nodes without reactions are uninspected`() {
        assertEquals(listOf(2), scene.uninspected(listOf(1, 2, MISSING)))
    }

    @Test
    fun `received reactions fill the missing ones, and never replace those of a frame`() {
        val filled: ViewportScene = scene.withMissingReactions(mapOf(1 to listOf("stale"), 2 to listOf("a", "b")))
        assertEquals<List<List<String>?>>(listOf(listOf("fresh"), listOf("a", "b")), filled.nodes.map { it.reactions })
    }

    @Test
    fun `a group inspector carries the reactions of each node`() {
        val inspector = assertIs<NodeGroup>(scene.toInspectorState(listOf(1, 2)))
        assertEquals(mapOf(1 to listOf("fresh"), 2 to null), inspector.reactionsByNode)
    }

    private companion object {
        const val MISSING = 42
    }
}
