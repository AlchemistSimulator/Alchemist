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
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * A [SteeringAction] allowing a pedestrian to navigate an environment consciously (e.g. without getting stuck in
 * U-shaped obstacles). Names are inspired to indoor environments, but this interface works for outdoor ones as well.
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space the pedestrian is into.
 * @param A the transformations supported by the shapes in this space.
 * @param L the type of landmarks of the pedestrian's cognitive map.
 * @param R the type of edges of the pedestrian's cognitive map, representing the [R]elations between landmarks.
 * @param N the type of nodes of the navigation graph provided by the [environment].
 * @param E the type of edges of the navigation graph provided by the [environment].
 */
interface NavigationAction<T, P, A, L, R, N, E> : SteeringAction<T, P>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          L : ConvexGeometricShape<P, A>,
          N : ConvexGeometricShape<P, A> {

    /**
     * The pedestrian to move.
     */
    val pedestrian: OrientingPedestrian<T, P, A, L, R>

    /**
     * The environment the [pedestrian] is into.
     */
    val environment: EnvironmentWithGraph<*, T, P, A, N, E>

    /**
     * The position of the [pedestrian] in the [environment].
     */
    val pedestrianPosition: P

    /**
     * The room (= environment's area) the [pedestrian] is into.
     */
    val currentRoom: N?

    /**
     * @returns the doors (= passages/edges) the pedestrian can perceive.
     */
    fun doorsInSight(): List<E>

    /**
     * Moves the pedestrian across the provided [door], which must be among [doorsInSight].
     */
    fun crossDoor(door: E)

    /**
     * Moves the pedestrian to the given final [destination], which must be inside [currentRoom].
     */
    fun moveToFinal(destination: P)

    /**
     * Stops moving the pedestrian.
     */
    fun stop() = moveToFinal(pedestrianPosition)
}

/**
 * A [NavigationAction] in a bidimensional euclidean space.
 */
typealias NavigationAction2D<T, L, R, N, E> =
    NavigationAction<T, Euclidean2DPosition, Euclidean2DTransformation, L, R, N, E>
