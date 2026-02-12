/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.Vector

/**
 * A [SteeringAction] that exposes an absolute target position.
 *
 * @param T the concentration type.
 * @param P the [Position] and [Vector] type used by the environment.
 */
interface SteeringActionWithTarget<T, P> : SteeringAction<T, P> where P : Position<P>, P : Vector<P> {
    /**
     * Returns the absolute target position this action points to.
     *
     * @return the target position as a [P].
     */
    fun target(): P

    /**
     * Computes the distance between this action's target and the given [node] within the provided [environment].
     *
     * @param node the node to measure distance to.
     * @param environment the environment containing the node.
     * @return the distance from the action's target to the node's position.
     */
    fun targetDistanceTo(node: Node<T>, environment: Environment<T, P>): Double =
        target().distanceTo(environment.getPosition(node))
}
