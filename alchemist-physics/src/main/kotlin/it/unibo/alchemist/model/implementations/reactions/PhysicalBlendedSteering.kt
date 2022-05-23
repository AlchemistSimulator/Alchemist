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
import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian2D

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

    private var previouslyAppliedForce = Euclidean2DPosition.zero

    private val pedestrian = node.asProperty<T, PedestrianProperty<T>>()

    private val physics = node.asProperty<T, PhysicalPedestrian2D<T>>()

    override val steerStrategy: SteeringStrategy<T, Euclidean2DPosition> =
        Sum(environment, node, super.steerStrategy)

    /**
     * Update the node physical state.
     */
    override fun execute() {
        (actions - steerActions()).forEach { it.execute() }
        val force = steerStrategy.computeNextPosition(steerActions())
        previouslyAppliedForce += force
        val normalizedForce = if (force.magnitude > 0) force.normalized() else Euclidean2DPosition.zero
        val fallenAgentRepulsionForce = physics.fallenAgentAvoidanceForce().total()
        val repulsionForce = physics.repulsionForce().total()
        val fallenAgentAvoidancePriority = when {
            fallenAgentRepulsionForce.magnitude > 0 -> fallenAgentAvoidanceWeight
            else -> 0.0
        }
        /*
         * Represents whether the agent will move in this step in its desired direction
         * of movement or instead be pushed by a repulsion force.
         * See the work of Pelechano et al https://bit.ly/3e3C7Tb
         */
        val velocityFactor = if (repulsionForce.magnitude > 0) 0.0 else 1.0
        val velocity = (normalizedForce * (1.0 - fallenAgentAvoidancePriority)) +
            (fallenAgentRepulsionForce * fallenAgentAvoidancePriority) *
            pedestrian.speed() * velocityFactor + repulsionForce
        environment.setVelocity(node, velocity)
        environment.updatePhysics(1 / rate)
    }

    private fun List<Euclidean2DPosition>.total() = this.fold(Euclidean2DPosition.zero) { acc, f -> acc + f }

    companion object {
        private const val fallenAgentAvoidanceWeight = 0.5
    }
}
