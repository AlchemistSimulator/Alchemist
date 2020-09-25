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
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.nodes.NodeWithShape

/**
 * A [PhysicalPedestrian] in an euclidean bidimensional space. This pedestrian has a circular [comfortArea] of radius
 * equal to its shape radius plus a [comfortRay].
 * This is derived from [the work of Pelechano et al](https://bit.ly/3e3C7Tb).
 */
interface PhysicalPedestrian2D<T> :
    PhysicalPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, Euclidean2DShapeFactory>,
    Pedestrian2D<T> {

    /**
     * The comfort ray of this pedestrian, this is added to the radius of its [shape] to obtain the [comfortArea].
     */
    val comfortRay: Double

    /**
     * The comfort area of this pedestrian, it's a circle of radius [shape].radius + [comfortRay].
     */
    override val comfortArea: Euclidean2DShape get() = environment.shapeFactory.circle(shape.radius + comfortRay)

    /**
     * Computes the repulsion force caused by a node that entered the [comfortArea]. This is derived from the work
     * of [Pelechano et al](https://bit.ly/3e3C7Tb).
     */
    override fun repulsionForce(other: NodeWithShape<T, Euclidean2DPosition, *>): Euclidean2DPosition =
        (shape.centroid - other.shape.centroid).let {
            val desiredDistance = shape.radius + comfortRay + other.shape.radius
            /*
             * it is the vector leading from the center of other to the center of this node, it.magnitude is the
             * actual distance between the two nodes.
             */
            it.normalized() * (desiredDistance - it.magnitude).coerceAtLeast(0.0) / it.magnitude
        }
}
