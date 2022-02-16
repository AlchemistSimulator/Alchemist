/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.capabilities

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
interface PedestrianPhysicalCapability<T, P, A, F> : PhysicalCapability<T, P, A, F>
    where P : Position<P>, P : Vector<P>,
          A : GeometricTransformation<P>,
          F : GeometricShapeFactory<P, A> {

    /**
     * The comfort ray of this pedestrian, this is added to the radius of its [shape] to obtain the [comfortArea].
     */
    val comfortRay: Double

    /**
     * The comfort area of this pedestrian, it's a circle of radius [shape].radius + [comfortRay].
     */
    val comfortArea: GeometricShape<P, A>

    /**
     * Computes the repulsion force caused by a node that entered the [comfortArea]. This is derived from the work
     * of [Pelechano et al](https://bit.ly/3e3C7Tb).
     */
    fun repulsionForce(other: Node<T>): P
}

/**
 * A pedestrian's capability to experience physical forces in a 2D space.
 */
interface Pedestrian2DPhysicalCapability<T> :
    PhysicalCapability<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>
