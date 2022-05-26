/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.CognitiveAgentCombineSteering
import it.unibo.alchemist.model.implementations.actions.steeringstrategies.DistanceWeighted
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.properties.Physical2D
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.Time
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty

/**
 * A reaction for the update of a [Dynamics2DEnvironment].
 */
class Physical<T>(
    private val environment: Dynamics2DEnvironment<T>,
    node: Node<T>,
    timeDistribution: TimeDistribution<T>,
) : AbstractReaction<T>(node, timeDistribution) {
    /**
     *
     */
    override fun updateInternalStatus(
        currentTime: Time?,
        hasBeenExecuted: Boolean,
        environment: Environment<T, *>?
    ) = Unit

    private val nodePhysics = node.asProperty<T, Physical2D<T>>()

    /**
     *
     */
    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): Physical<T> =
        Physical(environment, node, timeDistribution)

    /**
     *
     */
    override fun getRate(): Double = timeDistribution.rate

    private fun steerActions() = actions.filterIsInstance<SteeringAction<T, Euclidean2DPosition>>()

    /**
     * Update the [environment] physical state.
     */
    override fun execute() {
        (actions - steerActions()).forEach { it.execute() }
        val velocity = CognitiveAgentCombineSteering(
            environment,
            this,
            node,
            steerActions(),
            DistanceWeighted(environment, node),
        ).nextPosition
        environment.setVelocity(node, velocity)
        environment.updatePhysics(1 / rate)
    }
}
