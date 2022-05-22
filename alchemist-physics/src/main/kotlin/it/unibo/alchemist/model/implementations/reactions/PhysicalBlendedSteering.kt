/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.implementations.actions.physicalstrategies.Sum
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.SteeringStrategy
import it.unibo.alchemist.model.interfaces.TimeDistribution
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.interfaces.properties.PedestrianProperty
import it.unibo.alchemist.model.interfaces.Node.Companion.asProperty

/**
 * A [BlendedSteering] reaction which also considers physical interactions.
 */
class PhysicalBlendedSteering<T>(
    /**
     * The environment in which the node is moving.
     */
    val environment: Dynamics2DEnvironment<T>,
    node: Node<T>,
    timeDistribution: TimeDistribution<T>,
) : BlendedSteering<T>(environment, node, timeDistribution) {

    override val steerStrategy: SteeringStrategy<T, Euclidean2DPosition> =
        Sum(environment, node, super.steerStrategy)

    /**
     * Update the node physical state.
     */
    override fun execute() {
        (actions - steerActions()).forEach { it.execute() }
        val velocity = steerStrategy
            .computeNextPosition(steerActions())
            .coerceAtMost(node.asProperty<T, PedestrianProperty<T>>().speed())
        environment.setVelocity(node, velocity)
        environment.updatePhysics(1 / rate)
    }
}
