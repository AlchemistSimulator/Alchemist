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
 * Move the node towards the target position as fast as possible.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param pedestrian
 *          the owner of this action.
 * @param target
 *          the position the node moves towards.
 */
open class CognitiveAgentSeek<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
    target: P,
) : CognitiveAgentArrive<T, P, A>(environment, reaction, pedestrian, 0.0, 0.0, target)
    where P : Position<P>, P : Vector<P>,
          A : Transformation<P> {

    constructor(
        environment: Environment<T, P>,
        reaction: Reaction<T>,
        pedestrian: PedestrianProperty<T>,
        vararg coordinates: Number,
    ) : this(environment, reaction, pedestrian, environment.makePosition(*coordinates))

    override fun cloneAction(node: Node<T>, reaction: Reaction<T>): CognitiveAgentSeek<T, P, A> =
        CognitiveAgentSeek(environment, reaction, node.pedestrianProperty, target)
}
