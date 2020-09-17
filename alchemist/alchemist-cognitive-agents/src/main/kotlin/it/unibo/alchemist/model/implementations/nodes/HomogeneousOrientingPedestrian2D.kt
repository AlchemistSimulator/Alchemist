/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Ellipse
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.OrientingPedestrian
import it.unibo.alchemist.model.interfaces.Pedestrian2D
import it.unibo.alchemist.model.interfaces.PedestrianGroup2D
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.nextDouble
import org.apache.commons.math3.random.RandomGenerator
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

private typealias AbstractOrientingPedestrian2D<T, L, N, E> =
    AbstractOrientingPedestrian<T, Euclidean2DPosition, Euclidean2DTransformation, L, N, E, Euclidean2DShapeFactory>

/**
 * A homogeneous [OrientingPedestrian] in the Euclidean world. Landmarks are represented as [Ellipse]s, which can
 * model the human error concerning both the exact position of a landmark and the angles formed by the connections
 * between them. This class accepts an environment whose graph contains [ConvexPolygon]al nodes.
 *
 * @param T the concentration type.
 * @param N the type of nodes of the navigation graph provided by the environment.
 * @param E the type of edges of the navigation graph provided by the environment.
 */
open class HomogeneousOrientingPedestrian2D<T, N : ConvexPolygon, E> @JvmOverloads constructor(
    override val environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, E>,
    randomGenerator: RandomGenerator,
    knowledgeDegree: Double,
    /**
     * The starting width and height of the generated Ellipses will be a random quantity in
     * ([minSide, maxSide] * the diameter of this pedestrian).
     */
    private val minSide: Double = 30.0,
    private val maxSide: Double = 60.0,
    group: PedestrianGroup2D<T>? = null
) : AbstractOrientingPedestrian2D<T, Ellipse, N, E>(
    knowledgeDegree,
    randomGenerator,
    environment,
    group
),
    Pedestrian2D<T> {

    override val shape by lazy { super.shape }
    final override val fieldOfView by lazy { super.fieldOfView }

    init {
        senses += fieldOfView
    }

    override fun createLandmarkIn(area: N): Ellipse = with(area) {
        val width = randomEllipseSide()
        val height = randomEllipseSide()
        val frame = Rectangle2D.Double(centroid.x, centroid.y, width, height)
        while (!contains(frame)) {
            frame.width /= 2
            frame.height /= 2
        }
        Ellipse(Ellipse2D.Double(frame.x, frame.y, frame.width, frame.height))
    }

    private fun randomEllipseSide(): Double = randomGenerator.nextDouble(minSide, maxSide) * shape.diameter
}
