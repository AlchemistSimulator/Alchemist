/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.properties

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.geometry.GeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation

/**
 * A pedestrian's capability to experience physical forces.
 */
interface PhysicalPedestrian<T, P, A, F> : PhysicalProperty<T, P, A, F>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {
    /**
     * Whether the pedestrian is fallen and thus an obstacle.
     */
    val isFallen: Boolean get() = false

    /**
     * The comfort ray of this pedestrian, this is added to the radius of its [shape] to obtain the [comfortArea].
     */
    val comfortRay: Double

    /**
     * The comfort area of this pedestrian, it's a circle of radius [shape].radius + [comfortRay].
     */
    val comfortArea: GeometricShape<P, A>

    /**
     * Rectangle of influence. When a pedestrian enters this area, the node could experience a tangential
     * avoidance force. See [avoid].
     */
    val rectangleOfInfluence: GeometricShape<P, A>

    /**
     * Computes the repulsion force caused by a node that entered the [comfortArea]. This is derived from the work
     * of [Pelechano et al](https://bit.ly/3e3C7Tb).
     */
    fun repulse(other: Node<T>): P

    /**
     * Computes the repulsion force caused by a node that entered the [rectangleOfInfluence]. This is derived from
     * the work of [Pelechano et al](https://bit.ly/3e3C7Tb).
     */
    fun avoid(other: Node<T>): P

    /**
     * Computes the total repulsion force this node is subject to.
     */
    fun repulsionForce(): List<P>

    /**
     * Computes the total avoidance force this node is subject to.
     */
    fun avoidanceForce(): List<P>

    /**
     * Computes the avoidance force from a fallen pedestrian.
     */
    fun fallenAgentAvoidanceForce(): List<P>
}

/**
 * A pedestrian's capability to experience physical forces in a 2D space.
 */
interface PhysicalPedestrian2D<T> :
    PhysicalPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>
