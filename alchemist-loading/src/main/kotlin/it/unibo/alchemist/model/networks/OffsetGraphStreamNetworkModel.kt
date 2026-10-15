/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.networks

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Neighborhood
import it.unibo.alchemist.model.NetworkModel
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.observation.Observable
import kotlin.streams.toList
import org.graphstream.graph.Graph

/**
 * A [NetworkModel] that statically connects the nodes of [environment] as they were configured by GraphStream.
 * An [offset] is used to determine the id of the environment's nodes when compared to the one of the
 * provided [graph].
 */
class OffsetGraphStreamNetworkModel<T, P : Position<P>>(
    private val environment: Environment<T, P>,
    val offset: Int,
    val graph: Graph,
) : NetworkModel<T> {
    // The neighbors are linked as soon as they are part of the environment.
    override fun neighborhoodOf(center: Node<T>): Observable<Neighborhood<T>> {
        val actualId = center.id - offset
        val graphNode = if (actualId >= 0 && actualId < graph.nodeCount) graph.getNode(actualId) else null
        val neighborsIds = graphNode
            ?.neighborNodes()
            ?.mapToInt { it.index + offset }
            ?.toList()
            .orEmpty()
            .toSet()
        return environment.nodes.map { nodes ->
            Neighborhood(center, nodes.filter { it.id in neighborsIds })
        }
    }

    override fun toString(): String = "${javaClass.simpleName}($offset)"
}
