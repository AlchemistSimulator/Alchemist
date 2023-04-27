/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.reactions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.cognitive.actions.CognitiveAgentCombineSteering
import it.unibo.alchemist.model.cognitive.actions.steeringstrategies.DistanceWeighted
import it.unibo.alchemist.model.interfaces.environments.Dynamics2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.AbstractReaction

/**
 * A reaction for the update of a [Dynamics2DEnvironment].
 */
class Physical<T>(
    private val environment: Dynamics2DEnvironment<T>,
    private val pedestrian: PedestrianProperty<T>,
    timeDistribution: TimeDistribution<T>,
) : AbstractReaction<T>(pedestrian.node, timeDistribution) {
    /**
     * Update the internal status of the reaction.
     */
    override fun updateInternalStatus(
        currentTime: Time?,
        hasBeenExecuted: Boolean,
        environment: Environment<T, *>?,
    ) = Unit

    /**
     * Clones this reaction into [node].
     */
    override fun cloneOnNewNode(node: Node<T>, currentTime: Time): Physical<T> =
        Physical(environment, node.asProperty(), timeDistribution)

    private fun steerActions() = actions.filterIsInstance<SteeringAction<T, Euclidean2DPosition>>()

    /**
     * Update the [environment] physical state.
     */
    override fun execute() {
        (actions - steerActions()).forEach { it.execute() }
        val velocity = CognitiveAgentCombineSteering(
            environment,
            this,
            pedestrian,
            steerActions(),
            DistanceWeighted(environment, node),
        ).nextPosition
        environment.setVelocity(node, velocity)
        if (velocity.magnitude > 0) {
            environment.setHeading(node, velocity.normalized())
        }
        environment.updatePhysics(1 / rate)
    }
}
