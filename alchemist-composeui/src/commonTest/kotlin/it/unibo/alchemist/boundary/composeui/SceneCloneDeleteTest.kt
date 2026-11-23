/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui

import it.unibo.alchemist.boundary.composeui.model.Labeled
import it.unibo.alchemist.boundary.composeui.model.ViewportNode
import it.unibo.alchemist.boundary.composeui.model.ViewportScene
import it.unibo.alchemist.boundary.composeui.model.drag.NodePositionUpdate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.collections.immutable.persistentListOf

class SceneCloneDeleteTest {
    private val scene = ViewportScene(
        nodes = persistentListOf(
            ViewportNode(
                id = 3,
                coordinates = persistentListOf(0.0, 0.0),
                concentrations = persistentListOf(Labeled("x", 1.0)),
            ),
            ViewportNode(id = 7, coordinates = persistentListOf(1.0, 1.0)),
        ),
    )

    @Test
    fun `clones get new ids, the contents of their originals, and the given positions`() {
        val cloned = scene.withClonedNodes(listOf(NodePositionUpdate(3, persistentListOf(5.0, 6.0))))
        val clone = cloned.nodes.last()
        assertEquals(8, clone.id)
        assertEquals(listOf(5.0, 6.0), clone.coordinates)
        assertEquals(scene.nodes.first().concentrations, clone.concentrations)
    }

    @Test
    fun `deleting nodes keeps the others`() {
        assertEquals(listOf(7), scene.withoutNodes(listOf(3)).nodes.map { it.id })
    }
}
