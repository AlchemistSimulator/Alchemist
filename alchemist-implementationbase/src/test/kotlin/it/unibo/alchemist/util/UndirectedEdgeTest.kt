/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import it.unibo.alchemist.model.environments.Continuous2DEnvironment
import it.unibo.alchemist.model.linkingrules.ConnectWithinDistance
import it.unibo.alchemist.model.nodes.GenericNode
import it.unibo.alchemist.model.protelis.ProtelisIncarnation
import it.unibo.alchemist.util.Environments.UndirectedEdge
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class UndirectedEdgeTest {
    @Test
    fun `UndirectedEdge should be idependent from the source and destination nodes`() {
        Continuous2DEnvironment(ProtelisIncarnation()).apply {
            linkingRule = ConnectWithinDistance(5.0)
            val node1 = GenericNode(ProtelisIncarnation(), this)
            val node2 = GenericNode(ProtelisIncarnation(), this)
            assertEquals(UndirectedEdge(node1, node2), UndirectedEdge(node1, node2))
            assertEquals(UndirectedEdge(node1, node2), UndirectedEdge(node2, node1))
            assertEquals(UndirectedEdge(node1, node2).hashCode(), UndirectedEdge(node2, node1).hashCode())
        }
    }
}
