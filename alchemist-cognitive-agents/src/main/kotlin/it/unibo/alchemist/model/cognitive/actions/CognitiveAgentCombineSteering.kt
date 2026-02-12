/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
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
import it.unibo.alchemist.model.cognitive.SteeringAction
import it.unibo.alchemist.model.cognitive.SteeringStrategy
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * Combines multiple steering actions into a single steering behavior.
 *
 * @constructor Creates a new composite steering action.
 * @param environment the environment in which the node moves.
 * @param reaction the reaction that executes this action.
 * @param pedestrian the owner of this action.
 * @param actions the list of steering actions to combine.
 * @param steerStrategy the strategy used to combine the steering actions.
 * @param T the type of the concentration.
 * @param P the type of the position.
 * @param A
 */
class CognitiveAgentCombineSteering<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
    private val actions: List<SteeringAction<T, P>>,
    private val steerStrategy: SteeringStrategy<T, P>,
) : AbstractSteeringAction<T, P, A>(environment, reaction, pedestrian)
    where P : Position<P>, P : Vector<P>,
          A : Transformation<P> {
    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentCombineSteering<T, P, A> =
        CognitiveAgentCombineSteering(environment, reaction, node.pedestrianProperty, actions, steerStrategy)

    override fun nextPosition(): P = steerStrategy.computeNextPosition(actions).coerceAtMost(maxWalk)
}
