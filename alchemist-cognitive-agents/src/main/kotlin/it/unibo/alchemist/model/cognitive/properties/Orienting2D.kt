/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.cognitive.properties

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.cognitive.OrientingProperty
import it.unibo.alchemist.model.euclidean.environments.Euclidean2DEnvironmentWithGraph
import it.unibo.alchemist.model.euclidean.geometry.ConvexPolygon
import it.unibo.alchemist.model.euclidean.geometry.Ellipse
import it.unibo.alchemist.model.euclidean.geometry.Euclidean2DTransformation
import it.unibo.alchemist.model.euclidean.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.properties.AreaProperty
import it.unibo.alchemist.util.RandomGenerators.nextDouble
import org.apache.commons.math3.random.RandomGenerator
import org.jgrapht.graph.DefaultEdge
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

/**
 * Basic implementation of a node's [OrientingProperty] in a 2D space.
 */
class Orienting2D<T, N : ConvexPolygon> @JvmOverloads constructor(
    override val environment: Euclidean2DEnvironmentWithGraph<*, T, N, DefaultEdge>,
    randomGenerator: RandomGenerator,
    override val node: Node<T>,
    override val knowledgeDegree: Double,
    /**
     * The starting width and height of the generated Ellipses will be a random quantity in
     * ([minSide, maxSide] * the diameter of this pedestrian).
     */
    private val minSide: Double = 30.0,
    private val maxSide: Double = 60.0,
) : Orienting<T, Euclidean2DPosition, Euclidean2DTransformation, N, Ellipse>(
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
            node.asProperty<T, AreaProperty<T>>().shape.diameter

    override fun cloneOnNewNode(node: Node<T>) = Orienting2D(
        environment,
        randomGenerator,
        node,
        knowledgeDegree,
        minSide,
        maxSide,
    )
}
