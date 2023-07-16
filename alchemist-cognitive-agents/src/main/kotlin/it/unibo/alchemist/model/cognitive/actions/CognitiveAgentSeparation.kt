/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.PerceptiveProperty
import it.unibo.alchemist.model.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.physics.environments.Physics2DEnvironment
import it.unibo.alchemist.model.positions.Euclidean2DPosition

/**
 * Move the agent away from the pedestrians near to him.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 */
class CognitiveAgentSeparation<T>(
    val environment: Physics2DEnvironment<T>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
) : AbstractGroupSteeringAction<T, Euclidean2DPosition, Euclidean2DTransformation>(environment, reaction, pedestrian) {

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentSeparation<T> =
        CognitiveAgentSeparation(environment, reaction, node.pedestrianProperty)

    override fun nextPosition(): Euclidean2DPosition = (currentPosition - centroid()).coerceAtMost(maxWalk)

    override fun group(): List<Node<T>> = node.asProperty<T, PerceptiveProperty<T>>()
        .fieldOfView
        .influentialNodes()
        .plusElement(node)
}
