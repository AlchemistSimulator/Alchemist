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
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.util.Iterables.randomElement
import org.apache.commons.math3.random.RandomGenerator

/**
 * A local action applied to a neighbor of the node.
 *
 * @param T concentration type
 */
abstract class AbstractNeighborAction<T>(
    reaction: NodeReaction<T>,
    /**
     * The environment containing the node and its neighbors.
     */
    protected val environment: Environment<T, *>,
    randomGenerator: RandomGenerator,
) : AbstractRandomizableAction<T>(reaction, randomGenerator) {
    /**
     * Executes this action on a random neighbor, if the node has any; otherwise, does nothing.
     */
    override fun execute() {
        val neighborhood = environment.getNeighborhood(targetNode).current
        if (!neighborhood.isEmpty) {
            execute(neighborhood.randomElement(randomGenerator))
        }
    }

    /**
     * Executes this action on [neighbor], which is not guaranteed to belong to the neighborhood of [targetNode].
     */
    abstract fun execute(neighbor: Node<T>)
}
