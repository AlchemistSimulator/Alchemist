/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * Defines what a pedestrian should do when in a new room (= environment's area), this is designed to be used jointly
 * with a [NavigationAction]: the latter defines how to properly move the pedestrian, while delegating the decision on
 * where to move it to a [NavigationStrategy].
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space the pedestrian is into.
 * @param A the transformations supported by the shapes in this space.
 * @param L the type of landmarks of the pedestrian's cognitive map.
 * @param R the type of edges of the pedestrian's cognitive map, representing the [R]elations between landmarks.
 * @param N the type of nodes of the navigation graph provided by the environment.
 * @param E the type of edges of the navigation graph provided by the environment.
 */
interface NavigationStrategy<T, P, A, L, R, N, E>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          L : ConvexGeometricShape<P, A>,
          N : ConvexGeometricShape<P, A> {

    /**
     * The [NavigationAction] used to navigate the environment.
     */
    val action: NavigationAction<T, P, A, L, R, N, E>

    /**
     * This is called whenever the pedestrian enters a new room.
     */
    fun inNewRoom(newRoom: N)

    /**
     * This is called in place of [inNewRoom] when the pedestrian ends up in an unexpected room while moving.
     * By default, unexpected rooms are treated just like expected ones.
     */
    fun inUnexpectedNewRoom(previousRoom: N, expectedNewRoom: N, actualNewRoom: N) = inNewRoom(actualNewRoom)
}

/**
 * A [NavigationStrategy] in a bidimensional euclidean space.
 */
typealias NavigationStrategy2D<T, L, R, N, E> =
    NavigationStrategy<T, Euclidean2DPosition, Euclidean2DTransformation, L, R, N, E>
