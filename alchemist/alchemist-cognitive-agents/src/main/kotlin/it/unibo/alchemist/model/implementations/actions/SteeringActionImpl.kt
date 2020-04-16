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
 * An abstract [SteeringAction], deriving from [AbstractMoveNode].
 */
abstract class SteeringActionImpl<T, P>(
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
    /*
     * Keeping bounds as much general as we can (instead of using AbstractEuclideanPosition),
     * so as to allow [AbstractOrienting] to derive from this class.
     */
    where
        P : Position<P>,
        P : Vector<P> {

    /**
     * Next relative position.
     */
    override fun getNextPosition(): P = nextPosition()

    /**
     * The maximum distance the pedestrian can walk, this is a length.
     */
    protected fun maxWalk(): Double = pedestrian.speed() / reaction.rate

    /**
     * If the magnitude of the vector is greater than [maxWalk], a resized version with
     * magnitude equal to such quantity is returned. Otherwise, the original vector is
     * returned.
     */
    protected fun P.resizedToMaxWalkIfGreater(): P = when {
        magnitude <= maxWalk() -> this
        else -> resized(maxWalk())
    }
}
