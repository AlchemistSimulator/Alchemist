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
 * Defines what a node should do when it enters a new room (an area of the environment).
 *
 * This interface is intended to be used together with a [NavigationAction]: the action
 * provides movement primitives (how to move), while the strategy decides where to move.
 *
 * @param T the concentration type.
 * @param P the [Position] and [Vector] type used by the environment.
 * @param A the transformation type supported by shapes in the environment.
 * @param L the type of landmarks in the node's cognitive map.
 * @param R the type of edges in the node's cognitive map, representing relations between landmarks.
 * @param N the type of navigation-area shapes provided by the environment.
 * @param E the type of edges of the navigation graph provided by the environment.
 */
interface NavigationStrategy<T, P, A, L, R, N, E>
    where P : Position<P>, P : Vector<P>,
          A : Transformation<P>,
          L : ConvexShape<P, A>,
          N : ConvexShape<P, A> {
    /** The [NavigationAction] used to navigate the environment. */
    val action: NavigationAction<T, P, A, L, R, N, E>

    /** The node's orienting capability. */
    val orientingCapability get() = action.orientingProperty

    /**
     * Called whenever the node enters a new room.
     *
     * @param newRoom the room that the node has just entered.
     */
    fun inNewRoom(newRoom: N)

    /**
     * Called when the node ends up in an unexpected room while moving.
     * By default, unexpected rooms are treated the same as expected ones.
     *
     * @param previousRoom the room the node was in before moving.
     * @param expectedNewRoom the room the node was expected to enter.
     * @param actualNewRoom the room the node actually entered.
     */
    fun inUnexpectedNewRoom(previousRoom: N, expectedNewRoom: N, actualNewRoom: N) = inNewRoom(actualNewRoom)
}
