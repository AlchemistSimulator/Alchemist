/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.geometry.ConvexShape
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * A [SteeringAction] allowing a node to navigate an environment consciously (for example, avoiding
 * getting stuck in U-shaped obstacles). Names are inspired by indoor environments, but this
 * interface also works for outdoor scenarios.
 *
 * @param T the concentration type.
 * @param P the [Position] and [Vector] type used by the space the node occupies.
 * @param A the transformation type supported by the shapes in this space.
 * @param L the type of landmarks in the node's cognitive map.
 * @param R the type of edges in the node's cognitive map, representing relations between landmarks.
 * @param N the type of navigation-area shapes provided by the [environment].
 * @param E the type of edges (passages) of the navigation graph provided by the [environment].
 */
interface NavigationAction<T, P, A, L, R, N, E> :
    SteeringAction<T, P>
    where P : Position<P>, P : Vector<P>,
          A : Transformation<P>,
          L : ConvexShape<P, A>,
          N : ConvexShape<P, A> {
    /** The owner of this action. */
    val navigatingNode: Node<T>

    /** The [navigatingNode]'s orienting property. */
    val orientingProperty get() = navigatingNode.asProperty<T, OrientingProperty<T, P, A, L, N, E>>()

    /** The environment the [navigatingNode] is in. */
    val environment: EnvironmentWithGraph<*, T, P, A, N, E>

    /** The position of the [navigatingNode] in the [environment]. */
    val pedestrianPosition: P

    /** The room (environment area) the [navigatingNode] is in, if any. */
    val currentRoom: N?

    /**
     * Returns the doors (passages/edges) the node can perceive.
     *
     * @return a [List] of visible door edges of type [E].
     */
    fun doorsInSight(): List<E>

    /**
     * Moves the node across the provided [door], which must be among the doors returned by [doorsInSight].
     *
     * @param door the door (edge) to cross.
     */
    fun crossDoor(door: E)

    /**
     * Moves the node to the given final [destination], which must be inside [currentRoom].
     *
     * @param destination the final destination position inside the current room.
     */
    fun moveToFinal(destination: P)

    /** Stops the node by moving it to its current position. */
    fun stop() = moveToFinal(pedestrianPosition)
}
