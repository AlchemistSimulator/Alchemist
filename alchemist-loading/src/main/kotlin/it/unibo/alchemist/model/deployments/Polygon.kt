/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.deployments

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.GeoPosition
import it.unibo.alchemist.model.Position2D
import org.apache.commons.math3.random.RandomGenerator
import java.awt.geom.Area
import java.awt.geom.Path2D
import java.awt.geom.Rectangle2D

/**
 * Alias for using pairs as bidimensional points.
 */
private typealias Point2D = Pair<Number, Number>

/**
 * Creates a new Polygon with the given points.
 *
 * @param nodes the count of nodes that need to get displaced inside the polygon
 *
 * @param pointsInput the points of the polygon.
 * The class does not check for "malformed" polygons (e.g. with intersections).
 * If the provided points do not represent a valid polygon in bidimensional space, the behaviour of this class is
 * undefined. There polygon is closed automatically (there is no need to pass the first point also as last element).
 *
 */
open class Polygon<P : Position2D<out P>>(
    environment: Environment<*, P>,
    randomGenerator: RandomGenerator,
    nodes: Int,
    pointsInput: List<*>,
) : AbstractRandomDeployment<P>(environment, randomGenerator, nodes) {

    private val points: List<Point2D> = pointsInput.map {
        val error: () -> String = { "$it cannot get converted to Pair<out Number, out Number>" }
        when (it) {
            is Pair<*, *> -> {
                require(it.first is Number && it.second is Number, error)
                @Suppress("UNCHECKED_CAST")
                it as Point2D
            }
            is List<*> -> {
                require(it.size == 2 && it[0] is Number && it[1] is Number, error)
                Pair(it[0] as Number, it[1] as Number)
            }
            else -> throw IllegalArgumentException(error())
        }
    }

    /**
     * The polygon in which positions are generated.
     */
    protected val polygon: Area = Area(
        Path2D.Double().apply {
            moveTo(points.first().toPosition)
            points.asSequence().drop(1).forEach {
                lineTo(it.toPosition)
            }
            closePath()
        },
    )

    /**
     * The rectangular bounds of the polygon.
     */
    protected val bounds: Rectangle2D

    /**
     * True if this environment works with [GeoPosition].
     */
    protected val isOnMaps by lazy { environment.makePosition(0, 0) is GeoPosition }

    init {
        require(points.size > 3) {
            "At least three points are required for a polygonal deployment (provided: ${points.size}: $points)"
        }
        require(polygon.isPolygonal) {
            "The provided points ($points) do not supply a valid polygon"
        }
        bounds = polygon.bounds2D
    }

    /**
     * @param i
     * the node number
     * @return the position of the node
     */
    final override tailrec fun indexToPosition(i: Int): P = bounds
        .run {
            val (x, y) = Pair(randomDouble(minX, maxX), randomDouble(minY, maxY))
            if (isOnMaps) {
                environment.makePosition(y, x) // Latitude, Longitude
            } else {
                environment.makePosition(x, y)
            }
        }
        .takeIf { polygon.contains(it) } ?: indexToPosition(i)

    /**
     * Converts a Point2D to a [P].
     */
    protected val Point2D.toPosition: Position2D<out P>
        get() = environment.makePosition(first, second)

    /**
     * Moves the path to the specified [Position2D].
     */
    protected fun Path2D.Double.moveTo(destination: Position2D<*>) = moveTo(destination.x, destination.y)

    /**
     * Adds a line to the path, connecting it to the specified [Position2D].
     */
    protected fun Path2D.Double.lineTo(destination: Position2D<*>) = lineTo(destination.x, destination.y)

    /**
     * Checks if a [Position2D] is inside the Polygon.
     */
    protected fun Area.contains(target: Position2D<*>) = contains(target.x, target.y)
}
