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
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Reaction
import it.unibo.alchemist.model.cognitive.PedestrianProperty
import it.unibo.alchemist.model.cognitive.SteeringActionWithTarget
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.movestrategies.TargetSelectionStrategy

/**
 * A [SteeringActionWithTarget] in a vector space.
 *
 * @param environment
 *          the environment inside which the node moves.
 * @param pedestrian
 *          the owner of this action.
 * @param targetSelectionStrategy
 *          strategy selecting the next target.
 */
abstract class AbstractSteeringActionWithTarget<T, P, A>(
    environment: Environment<T, P>,
    reaction: Reaction<T>,
    override val pedestrian: PedestrianProperty<T>,
    private val targetSelectionStrategy: TargetSelectionStrategy<T, P>,
) : AbstractSteeringAction<T, P, A>(environment, reaction, pedestrian),
    SteeringActionWithTarget<T, P>
    where P : Position<P>, P : Vector<P>,
          A : Transformation<P> {

    constructor(
        environment: Environment<T, P>,
        reaction: Reaction<T>,
        pedestrian: PedestrianProperty<T>,
        target: P,
    ) : this(
        environment,
        reaction,
        pedestrian,
        TargetSelectionStrategy { target },
    )

    override fun target(): P = targetSelectionStrategy.target

    /**
     * @returns the next relative position. By default, the node tries to move towards its [target].
     */
    override fun nextPosition(): P = (target() - currentPosition).coerceAtMost(maxWalk)
}
