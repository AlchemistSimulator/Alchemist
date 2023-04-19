/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A [SteeringAction] with a defined target.
 */
interface SteeringActionWithTarget<T, P> : SteeringAction<T, P> where P : Position<P>, P : Vector<P> {

    /**
     * The position the owner of this action moves towards, in absolute coordinates.
     */
    fun target(): P

    /**
     * Computes the distance between this action's target and the given [node].
     */
    fun targetDistanceTo(node: Node<T>, environment: Environment<T, P>): Double =
        target().distanceTo(environment.getPosition(node))
}
