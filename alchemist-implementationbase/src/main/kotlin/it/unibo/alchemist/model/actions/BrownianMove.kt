/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import org.apache.commons.math3.random.RandomGenerator

/**
 * Moves the target [node] randomly, by at most [range] along each dimension at every execution.
 *
 * @param T concentration type
 * @param P position type
 */
class BrownianMove<T, P : Position<P>> @JvmOverloads constructor(
    private val environment: Environment<T, P>,
    reaction: Reaction<T>,
    node: Node<T> = reaction.host as? Node<T> ?: error(
        "${BrownianMove::class.simpleName} must either be provided with a node " +
            "or be owned by a reaction hosted by a node",
    ),
    private val randomGenerator: RandomGenerator,
    private val range: Double,
) : AbstractNodeAction<T>(reaction, node) {
    override fun cloneTargeting(newReaction: Reaction<T>, newTarget: Node<T>): BrownianMove<T, P> =
        BrownianMove(environment, newReaction, newTarget, randomGenerator, range)

    override fun execute() {
        val displacement = environment.makePosition(randomOffset() * range, randomOffset() * range)
        environment.moveNodeTo(targetNode, environment.getCurrentPosition(targetNode).plus(displacement.coordinates))
    }

    private fun randomOffset(): Double = randomGenerator.nextFloat() - 0.5
}
