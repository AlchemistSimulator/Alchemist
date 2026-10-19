/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.actions

import it.unibo.alchemist.model.Action
import it.unibo.alchemist.model.NodeReaction
import it.unibo.alchemist.model.actions.AbstractLocalAction
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import org.apache.commons.math3.random.RandomGenerator

/**
 * Changes the heading of the node owning this action randomly.
 * The [environment] must support node heading, hence, be a [Physics2DEnvironment].
 */
class HeadTowardRandomDirection<T>(
    reaction: NodeReaction<T>,
    private val environment: Physics2DEnvironment<T>,
    private val randomGenerator: RandomGenerator,
) : AbstractLocalAction<T>(reaction) {
    override fun cloneOnNodeReaction(newReaction: NodeReaction<T>): Action<T> =
        HeadTowardRandomDirection(newReaction, environment, randomGenerator)

    /**
     * Changes the heading of the node randomly.
     */
    override fun execute() {
        val delta = PI_8 * (2 * randomGenerator.nextDouble() - 1)
        val originalAngle = environment.getHeading(targetNode).asAngle()
        environment.setHeading(targetNode, (originalAngle + delta).toDirection())
    }

    private fun Euclidean2DPosition.asAngle() = atan2(y, x)

    private fun Double.toDirection() = Euclidean2DPosition(cos(this), sin(this))

    private companion object {
        private const val PI_8 = Math.PI / 8
    }
}
