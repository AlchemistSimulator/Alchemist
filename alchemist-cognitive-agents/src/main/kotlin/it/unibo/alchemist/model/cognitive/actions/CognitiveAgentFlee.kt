/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * Move the agent away from a target position. It's the opposite of [CognitiveAgentSeek].
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param coords
 *          the coordinates of the position the node moves away.
 */
open class CognitiveAgentFlee<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
    vararg coords: Double,
) : AbstractSteeringAction<T, P, A>(environment, reaction, pedestrian)
    where P : Position<P>, P : Vector<P>,
          A : Transformation<P> {

    private val danger: P = environment.makePosition(*coords.toTypedArray())

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentFlee<T, P, A> =
        CognitiveAgentFlee(environment, reaction, node.pedestrianProperty, *danger.coordinates)

    override fun nextPosition(): P = (currentPosition - danger).resized(maxWalk)
}
