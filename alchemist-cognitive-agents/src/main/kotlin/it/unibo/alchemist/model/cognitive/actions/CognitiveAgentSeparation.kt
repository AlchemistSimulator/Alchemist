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
 * Steering action that moves the agent away from nearby pedestrians (separation behavior).
 *
 * @param T the concentration type.
 * @property environment the physics environment in which the node moves.
 * @param reaction the reaction executing this action.
 * @property pedestrian the owner pedestrian property.
 */
class CognitiveAgentSeparation<T>(
    val environment: Physics2DEnvironment<T>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
) : AbstractGroupSteeringAction<T, Euclidean2DPosition, Euclidean2DTransformation>(environment, reaction, pedestrian) {
    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentSeparation<T> =
        CognitiveAgentSeparation(environment, reaction, node.pedestrianProperty)

    override fun nextPosition(): Euclidean2DPosition = (currentPosition - centroid()).coerceAtMost(maxWalk)

    override fun group(): List<Node<T>> = node
        .asProperty<T, PerceptiveProperty<T>>()
        .fieldOfView
        .influentialNodes()
        .plusElement(node)
}
