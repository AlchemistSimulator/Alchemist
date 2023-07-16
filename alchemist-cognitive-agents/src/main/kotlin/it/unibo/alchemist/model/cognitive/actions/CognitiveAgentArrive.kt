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
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.impact.individual.Speed
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * Move the agent towards a target position.
 * It is similar to [CognitiveAgentSeek] but attempts to arrive at the target position with a zero velocity.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param reaction
 *          the reaction which executes this action.
 * @param pedestrian
 *          the owner of this action.
 * @param decelerationRadius
 *          the distance from which the node starts to decelerate.
 * @param arrivalTolerance
 *          the distance at which the node is considered arrived to the target.
 * @param target
 *          the position the node moves towards.
 */
open class CognitiveAgentArrive<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
    protected val decelerationRadius: Double,
    protected val arrivalTolerance: Double,
    protected val target: P,
) : AbstractSteeringActionWithTarget<T, P, A>(environment, reaction, pedestrian, target)
    where P : Position<P>,
          P : Vector<P>,
          A : Transformation<P> {

    constructor(
        environment: Environment<T, P>,
        reaction: Reaction<T>,
        pedestrian: PedestrianProperty<T>,
        decelerationRadius: Double,
        arrivalTolerance: Double,
        vararg coordinates: Number,
    ) : this(
        environment,
        reaction,
        pedestrian,
        decelerationRadius,
        arrivalTolerance,
        environment.makePosition(*coordinates),
    )

    override val maxWalk: Double get() = with((currentPosition as Vector<P>).distanceTo(target)) {
        when {
            this < arrivalTolerance -> 0.0
            this < decelerationRadius -> Speed.default * this / decelerationRadius / reaction.rate
            else -> node.asProperty<T, PedestrianProperty<T>>().speed() / reaction.rate
        }
    }

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentArrive<T, P, A> =
        CognitiveAgentArrive(
            environment,
            reaction,
            node.pedestrianProperty,
            decelerationRadius,
            arrivalTolerance,
            target,
        )
}
