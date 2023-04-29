/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.reactions

import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.SteeringStrategy
import it.unibo.alchemist.model.cognitive.steering.Sum
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.PhysicsDependency
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.interfaces.properties.PhysicalPedestrian2D

/**
 * A [BlendedSteering] reaction which also considers physical interactions.
 */
class PhysicalBlendedSteering<T>(
    /**
     * The environment in which the node is moving.
     */
    val environment: Dynamics2DEnvironment<T>,
    override val pedestrian: PedestrianProperty<T>,
    timeDistribution: TimeDistribution<T>,
) : BlendedSteering<T>(environment, pedestrian, timeDistribution) {

    private var previouslyAppliedForce = Euclidean2DPosition.zero

    private val physics = node.asProperty<T, PhysicalPedestrian2D<T>>()

    override val steerStrategy: SteeringStrategy<T, Euclidean2DPosition> =
        Sum(environment, node, super.steerStrategy)

    init {
        addInboundDependency(PhysicsDependency)
    }

    /**
     * Update the node physical state.
     */
    override fun execute() {
        (actions - steerActions()).forEach { it.execute() }
        val force = steerStrategy.computeNextPosition(steerActions())
        if (!physics.isFallen) {
            previouslyAppliedForce += force
            physics.checkAndPossiblyFall()
            val velocity = computeNewVelocity(force)
            environment.setVelocity(node, velocity)
            if (velocity.magnitude > 0) {
                environment.setHeading(node, velocity.normalized())
            }
        } else {
            environment.setVelocity(node, Euclidean2DPosition.zero)
        }
    }

    private fun computeNewVelocity(force: Euclidean2DPosition): Euclidean2DPosition {
        var normalizedForce = if (force.magnitude > 0) force.normalized() else Euclidean2DPosition.zero
        var fallenAgentAvoidanceForce = physics.fallenAgentAvoidanceForces().total()
        if (fallenAgentAvoidanceForce.magnitude > 0) {
            normalizedForce *= 1.0 - fallenAgentAvoidanceForceWeight
            fallenAgentAvoidanceForce = fallenAgentAvoidanceForce.normalized() * fallenAgentAvoidanceForceWeight
        }
        val repulsionForce = physics.repulsionForces().total()
        /*
         * Determine whether the agent will move in this step in its desired direction
         * of movement or instead be pushed by a repulsion force.
         * See the work of Pelechano et al https://bit.ly/3e3C7Tb
         */
        return if (repulsionForce.magnitude > 0) {
            repulsionForce
        } else {
            (normalizedForce + fallenAgentAvoidanceForce) * pedestrian.speed()
        }
    }

    private fun List<Euclidean2DPosition>.total() = this.fold(Euclidean2DPosition.zero) { acc, f -> acc + f }

    companion object {
        private const val fallenAgentAvoidanceForceWeight = 0.5
    }
}
