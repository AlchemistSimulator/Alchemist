/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions

import it.unibo.alchemist.model.EuclideanEnvironment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position2D
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.SteeringActionWithTarget
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector2D

/**
 * [CognitiveAgentSeek] behavior in a bidimensional environment, delegated to [CognitiveAgentFollowScalarField]
 * (this means the node tries to overtake others on its path,
 * in general its movements are more sophisticated than [CognitiveAgentSeek]).
 */
open class CognitiveAgentSeek2D<T, P, A>(
    /**
     * The environment the node is into.
     */
    protected val environment: EuclideanEnvironment<T, P>,
    reaction: Reaction<T>,
    final override val pedestrian: PedestrianProperty<T>,
    /**
     * The position the node wants to reach.
     */
    private val target: P,
) : AbstractSteeringAction<T, P, A>(environment, reaction, pedestrian),
    SteeringActionWithTarget<T, P>
    where P : Position2D<P>, P : Vector2D<P>,
          A : Transformation<P> {

    constructor(
        environment: EuclideanEnvironment<T, P>,
        reaction: Reaction<T>,
        pedestrian: PedestrianProperty<T>,
        x: Number,
        y: Number,
    ) : this(environment, reaction, pedestrian, environment.makePosition(x, y))

    private val followScalarField = CognitiveAgentFollowScalarField(environment, reaction, pedestrian, target) {
        -it.distanceTo(target)
    }

    override fun target(): P = target

    override fun nextPosition(): P = followScalarField.nextPosition()

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentSeek2D<T, P, A> =
        CognitiveAgentSeek2D(environment, reaction, node.pedestrianProperty, target)
}
