/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.ConvexShape
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * Defines what a node should do when in a new room (= environment's area), this is designed to be used jointly
 * with a [NavigationAction]: the latter defines how to properly move the node, while delegating the decision on
 * where to move it to a [NavigationStrategy].
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space the node is into.
 * @param A the transformations supported by the shapes in this space.
 * @param L the type of landmarks of the node's cognitive map.
 * @param R the type of edges of the node's cognitive map, representing the [R]elations between landmarks.
 * @param N the type of nodes of the navigation graph provided by the environment.
 * @param E the type of edges of the navigation graph provided by the environment.
 */
interface NavigationStrategy<T, P, A, L, R, N, E>
    where P : Position<P>, P : Vector<P>,
          A : Transformation<P>,
          L : ConvexShape<P, A>,
          N : ConvexShape<P, A> {

    /**
     * The [NavigationAction] used to navigate the environment.
     */
    val action: NavigationAction<T, P, A, L, R, N, E>

    /**
     * The node's orienting capability.
     */
    val orientingCapability get() = action.orientingProperty

    /**
     * This is called whenever the node enters a new room.
     */
    fun inNewRoom(newRoom: N)

    /**
     * This is called in place of [inNewRoom] when the node ends up in an unexpected room while moving.
     * By default, unexpected rooms are treated just like expected ones.
     */
    fun inUnexpectedNewRoom(previousRoom: N, expectedNewRoom: N, actualNewRoom: N) = inNewRoom(actualNewRoom)
}
