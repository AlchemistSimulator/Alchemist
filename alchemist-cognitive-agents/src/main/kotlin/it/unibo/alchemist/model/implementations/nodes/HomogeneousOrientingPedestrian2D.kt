/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.nodes

import it.unibo.alchemist.model.implementations.capabilities.BaseOrienting2DCapability
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Ellipse
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Node
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

private typealias Position2D = Euclidean2DPosition
private typealias Transformation2D = Euclidean2DTransformation
private typealias ShapeFactory = Euclidean2DShapeFactory

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
    randomGenerator: RandomGenerator,
    override val environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, E>,
    backingNode: Node<T>,
    knowledgeDegree: Double,
    /**
     * The starting width and height of the generated Ellipses will be a random quantity in
     * ([minSide, maxSide] * the diameter of this pedestrian).
     */
    private val minSide: Double = 30.0,
    private val maxSide: Double = 60.0,
    group: PedestrianGroup2D<T>? = null
) :
    Pedestrian2D<T>,
    AbstractOrientingPedestrian<T, Position2D, Transformation2D, Ellipse, N, E, ShapeFactory>(
        randomGenerator,
        environment,
        backingNode,
        knowledgeDegree,
        group,
    ) {

    @JvmOverloads constructor(
        incarnation: Incarnation<T, Euclidean2DPosition>,
        randomGenerator: RandomGenerator,
        environment: EuclideanPhysics2DEnvironmentWithGraph<*, T, N, E>,
        nodeCreationParameter: String? = null,
        knowledgeDegree: Double,
        minSide: Double = 30.0,
        maxSide: Double = 60.0,
        group: PedestrianGroup2D<T>? = null
    ) : this(
        randomGenerator,
        environment,
        incarnation.createNode(randomGenerator, environment, nodeCreationParameter),
        knowledgeDegree,
        minSide,
        maxSide,
        group,
    )

    init {
        backingNode.addCapability(
            BaseOrienting2DCapability<T, N, E>(
                randomGenerator,
                backingNode,
                knowledgeDegree,
                minSide,
                maxSide
            )
        )
    }

    /*
     * TODO: Should refer to capability
     */
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
