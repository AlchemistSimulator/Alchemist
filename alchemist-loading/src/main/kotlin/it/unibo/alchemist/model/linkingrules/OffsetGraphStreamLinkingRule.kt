/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.linkingrules

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.neighborhoods.Neighborhoods
import org.graphstream.graph.Graph
import kotlin.streams.toList

/**
 * A [LinkingRule] that statically connects nodes as they were configured by GraphStream.
 * An [offset] is used to determine the id of the environment's nodes when compared to the one of the
 * provided [graph].
 */
class OffsetGraphStreamLinkingRule<T, P : Position<P>>(val offset: Int, val graph: Graph) :
    LinkingRule<T, P> {

    override fun computeNeighborhood(center: Node<T>, environment: Environment<T, P>): Neighborhood<T> {
        val actualId = center.id - offset
        val graphNode = if (graph.nodeCount > actualId) graph.getNode(actualId) else null
        val neighborsIds = graphNode?.neighborNodes()?.mapToInt { it.index + offset }?.toList().orEmpty()
        val neighbors = if (neighborsIds.isEmpty()) {
            emptySequence()
        } else {
            environment.nodes.asSequence().filter { it.id in neighborsIds }
        }
        return Neighborhoods.make(environment, center, neighbors.asIterable())
    }

    override fun isLocallyConsistent() = true
}
