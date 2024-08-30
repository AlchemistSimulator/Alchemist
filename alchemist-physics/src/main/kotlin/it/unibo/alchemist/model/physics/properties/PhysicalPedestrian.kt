/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.physics.properties

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.geometry.Shape
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector

/**
 * A pedestrian's capability to experience physical forces.
 */
interface PhysicalPedestrian<T, P, A, F> : PhysicalProperty<T, P, A, F>
    where P : Position<P>, P : Vector<P>,
          A : Transformation<P>,
          F : GeometricShapeFactory<P, A> {

    /**
     * Whether the pedestrian has fallen and it is thus an obstacle.
     */
    val isFallen: Boolean

    /**
     * Turn this [node] into a fallen pedestrian if it [shouldFall].
     */
    fun checkAndPossiblyFall()

    /**
     * Determines if this pedestrian subject to [pushingForces] should fall.
     * According to the work of [Pelechano et al](https://doi.org/10.2312/SCA/SCA07/099-108)
     * this should happen when the majority of pushing forces affecting one individual are
     * approximately in the same direction and the sum of forces have a magnitude high
     * enough to make it lose equilibrium.
     */
    fun shouldFall(pushingForces: List<P>): Boolean

    /**
     * The comfort ray of this pedestrian, this is added to the radius of its [Shape] to obtain the [comfortArea].
     */
    val comfortRay: Double

    /**
     * The comfort area of this pedestrian, it's a circle of radius [Shape].radius + [comfortRay].
     */
    val comfortArea: Shape<P, A>

    /**
     * Rectangle of influence. When a pedestrian enters this area, the node could experience a tangential
     * avoidance force. See [avoid].
     */
    val rectangleOfInfluence: Shape<P, A>

    /**
     * Computes the repulsion force caused by a node that entered the [comfortArea]. This is derived from the work
     * of [Pelechano et al](https://doi.org/10.2312/SCA/SCA07/099-108).
     */
    fun repulse(other: Node<T>): P

    /**
     * Computes the repulsion force caused by a node that entered the [rectangleOfInfluence]. This is derived from
     * the work of [Pelechano et al](https://doi.org/10.2312/SCA/SCA07/099-108).
     */
    fun avoid(other: Node<T>): P

    /**
     * Computes the total repulsion force this node is subject to.
     */
    fun repulsionForces(): List<P>

    /**
     * Computes the total avoidance force this node is subject to.
     */
    fun avoidanceForces(): List<P>

    /**
     * Computes the avoidance force from a fallen pedestrian.
     */
    fun fallenAgentAvoidanceForces(): List<P>

    /**
     * Adds a listener to be called when [node] falls.
     */
    fun onFall(listener: (Node<T>) -> Unit)
}
