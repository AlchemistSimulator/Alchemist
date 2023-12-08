/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.reactions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.model.TimeDistribution
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.cognitive.SteeringStrategy
import it.unibo.alchemist.model.cognitive.actions.CognitiveAgentCombineSteering
import it.unibo.alchemist.model.positions.Euclidean2DPosition
import it.unibo.alchemist.model.reactions.AbstractReaction

/**
 * Reaction representing the steering behavior of a pedestrian.
 *
 * @param environment
 *          the environment inside which the pedestrian moves.
 * @param pedestrian
 *          the owner of this reaction.
 * @param timeDistribution
 *          the time distribution according to which this reaction executes.
 * @param steerStrategy
 *          the strategy used to combine steering actions.
 */
open class SteeringBehavior<T>(
    private val environment: Environment<T, Euclidean2DPosition>,
    /**
     * The pedestrian property of the owner of this reaction.
     */
    protected open val pedestrian: PedestrianProperty<T>,
    timeDistribution: TimeDistribution<T>,
    open val steerStrategy: SteeringStrategy<T, Euclidean2DPosition>,
) : AbstractReaction<T>(pedestrian.node, timeDistribution) {

    /**
     * The list of steering actions in this reaction.
     */
    fun steerActions(): List<SteeringAction<T, Euclidean2DPosition>> =
        actions.filterIsInstance<SteeringAction<T, Euclidean2DPosition>>()

    override fun cloneOnNewNode(node: Node<T>, currentTime: Time) =
        SteeringBehavior(environment, node.pedestrianProperty, timeDistribution, steerStrategy)

    override fun updateInternalStatus(
        currentTime: Time?,
        hasBeenExecuted: Boolean,
        environment: Environment<T, *>?,
    ) = Unit

    override fun execute() {
        (actions - steerActions().toSet()).forEach { it.execute() }
        CognitiveAgentCombineSteering(environment, this, pedestrian, steerActions(), steerStrategy).execute()
    }

    protected val Node<T>.pedestrianProperty get() = asProperty<T, PedestrianProperty<T>>()
}
