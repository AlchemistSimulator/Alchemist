/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.adapter

import it.unibo.alchemist.boundary.composeui.InfoField
import it.unibo.alchemist.boundary.composeui.SimulationStatus
import it.unibo.alchemist.boundary.composeui.ViewportEdge
import it.unibo.alchemist.boundary.composeui.ViewportNode
import it.unibo.alchemist.boundary.composeui.ViewportScene
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.core.Status
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position

fun <T, P : Position<P>> Node<T>.toViewport(environment: Environment<T, P>): ViewportNode = ViewportNode(
    id = id,
    coordinates = environment.getPosition(this).coordinates.toList(),
    concentrations = this.contents.map { InfoField(it.key.toString(), it.value.toString()) },
)

fun <T, P : Position<P>> Environment<T, P>.toViewport(): ViewportScene = ViewportScene(
    nodes = nodes.map { it.toViewport(this) },
    edges = extractEdges(),
    dimensions = dimensions,
)

fun <T, P : Position<P>> Simulation<T, P>.toSimulationStatus(): SimulationStatus = when (this.status) {
    Status.INIT -> SimulationStatus.INIT
    Status.READY -> SimulationStatus.READY
    Status.PAUSED -> SimulationStatus.PAUSED
    Status.RUNNING -> SimulationStatus.RUNNING
    Status.TERMINATED -> SimulationStatus.TERMINATED
}

private fun <T, P : Position<P>> Environment<T, P>.extractEdges(): List<ViewportEdge> = buildList {
    nodes.forEach { node ->
        getNeighborhood(node).forEach { neighbor ->
            if (node.id < neighbor.id) {
                add(ViewportEdge(node.id, neighbor.id))
            }
        }
    }
}
