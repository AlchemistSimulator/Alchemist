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
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Node.Companion.asPropertyOrNull
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.biochemistry.CellProperty
import it.unibo.alchemist.model.biochemistry.molecules.Junction
import org.apache.commons.math3.random.RandomGenerator

/**
 * Adds [junction] between a neighbor and the node, inside the neighbor only: the node is unaware of it.
 * [AddJunctionInCell] performs the other part of the junction creation.
 *
 * @param P position type
 */
class AddJunctionInNeighbor<P : Position<out P>>(
    environment: Environment<Double, P>,
    reaction: NodeReaction<Double>,
    private val junction: Junction,
    randomGenerator: RandomGenerator,
) : AbstractNeighborAction<Double>(reaction, environment, randomGenerator) {
    @Suppress("UNCHECKED_CAST") // The environment was provided with position type P at construction.
    override fun cloneOnNodeReaction(newReaction: NodeReaction<Double>): AddJunctionInNeighbor<P> {
        requireNotNull(newReaction.host.asPropertyOrNull<Double, CellProperty<*>>()) {
            "Node must have a ${CellProperty::class.simpleName}"
        }
        return AddJunctionInNeighbor(environment as Environment<Double, P>, newReaction, junction, randomGenerator)
    }

    /**
     * Fails: a junction cannot be created without a target node.
     */
    override fun execute(): Unit = throw UnsupportedOperationException(
        "A junction CAN NOT be created without a target node.",
    )

    /**
     * Creates the junction from [neighbor] to the node.
     */
    override fun execute(neighbor: Node<Double>) {
        if (neighbor.asPropertyOrNull<Double, CellProperty<*>>() == null) {
            throw UnsupportedOperationException(
                "Can't add Junction in a node with no ${CellProperty::class.simpleName}",
            )
        }
        neighbor.asProperty<Double, CellProperty<*>>().addJunction(junction, targetNode)
    }

    override fun toString(): String = "add junction $junction in neighbor"
}
