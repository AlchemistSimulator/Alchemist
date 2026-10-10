/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.biochemistry.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.molecules.Junction
import org.apache.commons.math3.random.RandomGenerator

/**
 * Removes [junction] between the node and a neighbor, inside the node only: the neighbor is unaware of it.
 * [RemoveJunctionInNeighbor] performs the other part of the junction removal.
 */
class RemoveJunctionInCell(
    environment: Environment<Double, *>,
    reaction: NodeReaction<Double>,
    private val junction: Junction,
    randomGenerator: RandomGenerator,
) : AbstractNeighborAction<Double>(reaction, environment, randomGenerator) {
    private val cell: CellProperty<*> = requireNotNull(targetNode.asPropertyOrNull<Double, CellProperty<*>>()) {
        "This Action can be set only in nodes with ${CellProperty::class.simpleName}"
    }

    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): RemoveJunctionInCell =
        RemoveJunctionInCell(environment, newReaction, junction, randomGenerator)

    /**
     * Does nothing: a junction cannot be removed without a target node.
     */
    override fun execute() = Unit

    /**
     * Removes the junction from the node to [neighbor].
     */
    override fun execute(neighbor: Node<Double>) {
        if (neighbor.asPropertyOrNull<Double, CellProperty<*>>() == null) {
            throw UnsupportedOperationException(
                "Can't remove Junction in a node with no ${CellProperty::class.simpleName}",
            )
        }
        cell.removeJunction(junction, neighbor)
    }

    override fun toString(): String = "remove junction $junction in cell"
}
