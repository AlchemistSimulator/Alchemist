/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces

import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector

/**
 * A [SteeringAction] allowing a pedestrian to navigate an environment consciously (e.g. without
 * getting stuck in U-shaped obstacles).
 * Names are inspired to indoor environments, but this interface works for outdoor ones as well.
 *
 * @param T the concentration type.
 * @param P the [Position] type and [Vector] type for the space the pedestrian is into.
 * @param A the transformations supported by the shapes in this space.
 * @param N the type of landmarks of the pedestrian's cognitive map.
 * @param E the type of edges of the pedestrian's cognitive map.
 * @param M the type of nodes of the navigation graph provided by the [environment].
 * @param F the type of edges of the navigation graph provided by the [environment].
 */
interface NavigationAction<T, P, A, N, E, M, F> : SteeringAction<T, P>
    where
        P : Position<P>, P : Vector<P>,
        A : GeometricTransformation<P>,
        N : ConvexGeometricShape<P, A>,
        M : ConvexGeometricShape<P, A> {

    /**
     * The pedestrian to move.
     */
    val pedestrian: OrientingPedestrian<T, P, A, N, E>

    /**
     * The environment the [pedestrian] is into.
     */
    val environment: EnvironmentWithGraph<*, T, P, A, M, F>

    /**
     * The position of the [pedestrian] in the [environment].
     */
    val pedestrianPosition: P

    /**
     * The room (= environment's area) the [pedestrian] is into.
     */
    val currentRoom: M?

    /**
     * @returns the doors (= passages/edges) the pedestrian can perceive.
     */
    fun doorsInSight(): List<F>

    /**
     * Moves the pedestrian across the provided [door], which must be among [doorsInSight].
     */
    fun crossDoor(door: F)

    /**
     * Moves the pedestrian to the given final [destination], which must be inside [currentRoom].
     */
    fun moveToFinal(destination: P)

    /**
     * Stops moving the pedestrian.
     */
    fun stop() = moveToFinal(pedestrianPosition)
}
