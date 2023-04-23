/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.actions

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.Context
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.environments.Physics2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import org.apache.commons.math3.random.RandomGenerator
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * Changes the heading of [node] randomly.
 * The [environment] must support node heading, hence, be a [Physics2DEnvironment].
 */
class HeadTowardRandomDirection<T>(
    node: Node<T>,
    private val environment: Physics2DEnvironment<T>,
    private val randomGenerator: RandomGenerator,
) : AbstractAction<T>(node) {

    /**
     * {@inheritDoc}.
     */
    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): Action<T> =
        HeadTowardRandomDirection(node, environment, randomGenerator)

    /**
     * Changes the heading of the node randomly.
     */
    override fun execute() {
        val delta = PI_8 * (2 * randomGenerator.nextDouble() - 1)
        val originalAngle = environment.getHeading(node).asAngle()
        environment.setHeading(node, (originalAngle + delta).toDirection())
    }

    /**
     * {@inheritDoc}.
     */
    override fun getContext() = Context.LOCAL

    @SuppressFBWarnings("SA_LOCAL_SELF_ASSIGNMENT")
    private fun Euclidean2DPosition.asAngle() = atan2(y, x)

    @SuppressFBWarnings("SA_LOCAL_SELF_ASSIGNMENT")
    private fun Double.toDirection() = Euclidean2DPosition(cos(this), sin(this))

    companion object {
        private const val PI_8 = Math.PI / 8
    }
}
