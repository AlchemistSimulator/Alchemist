/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Context
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.environments.Physics2DEnvironment
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
