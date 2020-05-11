/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * Defines what a pedestrian should do when in a new room (= environment's area), this is designed
 * to be used jointly with a [NavigationAction]: the latter defines how to properly move the
 * pedestrian, while delegating the decision on where to move it to a [NavigationStrategy].
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space the pedestrian is into.
 * @param A the transformations supported by the shapes in this space.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 * @param M the type of nodes of the navigation graph provided by the environment.
 * @param F the type of edges of the navigation graph provided by the environment.
 */
interface NavigationStrategy<T, P, A, N, E, M, F>
    where
        P : Position<P>, P : Vector<P>,
        A : GeometricTransformation<P>,
        N : ConvexGeometricShape<P, A>,
        M : ConvexGeometricShape<P, A> {

    /**
     * The [NavigationAction] used to navigate the environment.
     */
    val action: NavigationAction<T, P, A, N, E, M, F>

    /**
     * This is called whenever the pedestrian enters a new room.
     */
    fun inNewRoom(newRoom: M)

    /**
     * This is called in place of [inNewRoom] when the pedestrian ends up in an unexpected room while moving.
     * By default, unexpected rooms are treated just like expected ones.
     */
    fun inUnexpectedNewRoom(previousRoom: M, expectedNewRoom: M, actualNewRoom: M) = inNewRoom(actualNewRoom)
}
