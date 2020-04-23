/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.cognitiveagents.characteristics.cognitive.OrientingBehavior
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Action
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.SteeringAction
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DConvexShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage

/**
 * A [SteeringAction] representing the [OrientingBehavior] of a pedestrian.
 *
 * @param T the concentration type.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 * @param M the type of nodes of the navigation graph provided by the environment.
 */
open class Orienting<T, N : Euclidean2DConvexShape, E, M : ConvexPolygon>(
    private val environment: Euclidean2DEnvironmentWithGraph<*, T, M, Euclidean2DPassage>,
    reaction: Reaction<T>,
    pedestrian: OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, N, E>
) : AbstractSteeringAction<T, Euclidean2DPosition>(environment, reaction, pedestrian) {

    /**
     * The actual [OrientingBehavior].
     */
    val behavior: OrientingBehavior<T, N, E, M> = OrientingBehavior(environment, pedestrian)

    @Suppress("UNCHECKED_CAST")
    override fun cloneAction(n: Node<T>, r: Reaction<T>): Action<T> {
        require(n as? OrientingPedestrian<
            T, Euclidean2DPosition, Euclidean2DTransformation, M, Euclidean2DPassage> != null) {
            "node not compatible, required: " + pedestrian.javaClass + ", found: " + n.javaClass
        }
        n as OrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, M, Euclidean2DPassage>
        return Orienting(environment, r, n)
    }

    override fun nextPosition(): Euclidean2DPosition {
        behavior.update()
        return Seek2D(environment, reaction, pedestrian, *behavior.desiredPosition.coordinates).nextPosition
    }
}
