/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Pedestrian
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A [SteeringAction] in a vector space. The definition of [nextPosition] is left to
 * subclasses.
 */
abstract class AbstractSteeringAction<T, P>(
    env: Environment<T, P>,
    /**
     * The reaction in which this action is executed.
     */
    protected open val reaction: Reaction<T>,
    /**
     * The owner of this action.
     */
    protected open val pedestrian: Pedestrian<T>
) : AbstractMoveNode<T, P>(env, pedestrian), SteeringAction<T, P>
    where
        P : Position<P>,
        P : Vector<P> {

    /**
     * The maximum distance the pedestrian can walk, this is a length.
     */
    val maxWalk: Double get() = pedestrian.speed() / reaction.rate

    /**
     * Next relative position.
     */
    override fun getNextPosition(): P = nextPosition()
}
