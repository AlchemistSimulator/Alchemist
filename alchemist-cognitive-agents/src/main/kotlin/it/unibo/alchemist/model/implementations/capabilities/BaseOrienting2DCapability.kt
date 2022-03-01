/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Ellipse
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.Obstacle2D
import it.unibo.alchemist.model.interfaces.capabilities.OrientingCapability
import it.unibo.alchemist.model.interfaces.capabilities.Spatial2DCapability
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.environments.EuclideanPhysics2DEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.environments.PhysicsEnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.Euclidean2DPassage
import it.unibo.alchemist.nextDouble
import org.apache.commons.math3.random.RandomGenerator
import org.jgrapht.graph.DefaultEdge
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

private typealias Position2D = Euclidean2DPosition
private typealias Transformation2D = Euclidean2DTransformation
private typealias ShapeFactory = Euclidean2DShapeFactory

/**
 * Basic implementation of a node's [OrientingCapability] in a 2D space.
 */
class BaseOrienting2DCapability<T, N : ConvexPolygon>(
    override val environment: Euclidean2DEnvironmentWithGraph<*, T, N, DefaultEdge>,
    override val randomGenerator: RandomGenerator,
    override val node: Node<T>,
    override val knowledgeDegree: Double,
    /**
     * The starting width and height of the generated Ellipses will be a random quantity in
     * ([minSide, maxSide] * the diameter of this pedestrian).
     */
    private val minSide: Double = 30.0,
    private val maxSide: Double = 60.0,
) : BaseOrientingCapability<T, Position2D, Transformation2D, N, Ellipse>(
    randomGenerator,
    environment,
    node,
    knowledgeDegree,
) {
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

    private fun randomEllipseSide(): Double =
        randomGenerator.nextDouble(minSide, maxSide) *
            node.asCapability<T, Spatial2DCapability<T>>().shape.diameter
}
