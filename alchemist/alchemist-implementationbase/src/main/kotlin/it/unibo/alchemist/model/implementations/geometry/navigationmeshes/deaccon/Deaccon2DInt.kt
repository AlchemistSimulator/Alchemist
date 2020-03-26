/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.navigationmeshes.deaccon

import it.unibo.alchemist.model.implementations.geometry.createSegment
import it.unibo.alchemist.model.implementations.geometry.intersection
import it.unibo.alchemist.model.implementations.geometry.isAxisAligned
import it.unibo.alchemist.model.implementations.geometry.isXAxisAligned
import it.unibo.alchemist.model.implementations.geometry.toInterval
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.implementations.utils.RectObstacle2D
import it.unibo.alchemist.model.interfaces.geometry.navigationmeshes.deaccon.ExtendableConvexPolygon
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.MutableConvexPolygonImpl
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.advanceEdge
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.edgeClosestTo
import it.unibo.alchemist.model.implementations.geometry.findExtremePoints
import it.unibo.alchemist.model.implementations.geometry.subtractAll
import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.graph.Euclidean2DCrossing
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import it.unibo.alchemist.model.interfaces.graph.GraphEdgeWithData
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.awt.Shape
import java.awt.geom.Point2D

/**
 */
class Deaccon2DInt {

    private val origin = Euclidean2DPosition(0.0, 0.0)

    /**
     */
    fun generateEnvGraph(
        width: Int,
        height: Int,
        obstacles: List<RectObstacle2D>,
        rooms: List<Point2D>,
        crossingWidth: Int,
        destinations: List<Euclidean2DPosition>,
        mapper: (Euclidean2DPosition) -> Euclidean2DPosition
    ): Euclidean2DNavigationGraph {
        require(width > 0 && height > 0) { "width and height should be positive" }
        val seeds = rooms
            .map { createSeed(it.x, it.y) }
            .toMutableList()
            .grow(width, height, obstacles)
        val builder = Euclidean2DNavigationGraphBuilder()
        seeds.forEach { builder.addNode(it.mapPolygon(mapper)) }
        println(crossingWidth)
        seeds
            .findCrossings2(obstacles, width, height)
            //.findCrossings(crossingWidth.toDouble(), obstacles)
            .map { it.mapCrossing(mapper) }
            .filter {
                !fuzzyEquals(it.data.first.getDistanceTo(it.data.second), 0.0)
            }
            .forEach { builder.addEdge(it) }
        return builder.build(destinations)
    }

    private fun MutableList<ExtendableConvexPolygon>.grow(
        width: Int,
        height: Int,
        envObstacles: Collection<Shape>,
        step: Double = 1.0
    ): MutableList<ExtendableConvexPolygon> {
        removeIf { seed -> envObstacles.any { seed.intersects(it) } }
        val obstacles = envObstacles.toMutableList()
        obstacles.addAll(map { it.asAwtShape() })
        var growing = true
        while (growing) {
            growing = false
            forEachIndexed { i, seed ->
                /*
                 * each seed should not consider itself as an obstacle,
                 * thus it's removed and added back at the end
                 */
                obstacles.removeAt(envObstacles.size + i)
                val extended = seed.extend(step, obstacles, origin, width.toDouble(), height.toDouble())
                if (!growing) {
                    growing = extended
                }
                obstacles.add(envObstacles.size + i, seed.asAwtShape())
            }
        }
        return this
    }

    private fun MutableList<out ExtendableConvexPolygon>.findCrossings(
        crossingWidth: Double,
        envObstacles: Collection<Shape>
    ): Collection<Euclidean2DCrossing> {
        val crossings = mutableListOf<Euclidean2DCrossing>()
        forEach { seed ->
            seed.vertices().indices.forEach { i ->
                val edge = seed.getEdge(i)
                if (edge.isAxisAligned() && seed.advanceEdge(i, crossingWidth)) {
                    val intersectedSeeds = filter {
                        it != seed && it.intersects(seed.asAwtShape())
                    }
                    val intersectedObstacles = envObstacles.filter { seed.intersects(it) }
                    /*
                     * Moves the edge back to its previous position
                     */
                    seed.moveEdge(i, edge)
                    intersectedSeeds.forEach { neighbor ->
                        findCrossingsTo(neighbor, edge, intersectedObstacles)
                            .forEach {
                                crossings.add(GraphEdgeWithData(seed, neighbor, it))
                            }
                    }
                }
            }
        }
        return crossings
    }

    private fun MutableList<out ExtendableConvexPolygon>.findCrossings2(
        //crossingWidth: Double,
        envObstacles: Collection<Shape>,
        width: Int,
        height: Int
    ): Collection<Euclidean2DCrossing> {
        val crossings = mutableListOf<Euclidean2DCrossing>()
        forEach { seed ->
            seed.vertices().indices.forEach { i ->
                val edge = seed.getEdge(i)
                if (edge.isAxisAligned()) {
                    while (none { it != seed && it.intersects(seed.asAwtShape()) }) {
                        if (!seed.advanceEdge(i, 1.0, origin, width.toDouble(), height.toDouble())) {
                            break
                        }
                    }
                    val intersectedSeeds = filter {
                        it != seed && it.intersects(seed.asAwtShape())
                    }
                    val intersectedObstacles = envObstacles.filter { seed.intersects(it) }
                    /*
                     * Moves the edge back to its previous position
                     */
                    seed.moveEdge(i, edge)
                    intersectedSeeds.forEach { neighbor ->
                        findCrossingsTo(neighbor, edge, intersectedObstacles)
                            .forEach {
                                crossings.add(GraphEdgeWithData(seed, neighbor, it))
                            }
                    }
                }
            }
        }
        return crossings
    }

    private fun findCrossingsTo(
        neighbor: ConvexPolygon,
        advancedEdge: Euclidean2DSegment,
        intersectedObstacles: Collection<Shape>
    ): Collection<Euclidean2DSegment> {
        val neighborEdge = neighbor
            .edgeClosestTo(advancedEdge)
            .toInterval(advancedEdge.isXAxisAligned())
        advancedEdge.toInterval()
            .intersection(neighborEdge)
            ?.let { intersection ->
                val remaining = intersection.subtractAll(
                    /*
                     * Maps each obstacle to the interval of space it is occluding.
                     */
                    intersectedObstacles.map {
                        it.vertices().findExtremePoints(advancedEdge.isXAxisAligned())
                    }
                )
                return remaining.map {
                    if (advancedEdge.isXAxisAligned())
                        createSegment(it.first, advancedEdge.first.y, x2 = it.second)
                    else
                        createSegment(advancedEdge.first.x, it.first, y2 = it.second)
                }
            } ?: return mutableListOf()
    }

    private fun createSeed(x: Double, y: Double, side: Double = 1.0): ExtendableConvexPolygon =
        ExtendableConvexPolygonImpl(
            mutableListOf(
                Euclidean2DPosition(x, y),
                Euclidean2DPosition(x + side, y),
                Euclidean2DPosition(x + side, y + side),
                Euclidean2DPosition(x, y + side)
            )
        )

    private fun Euclidean2DCrossing.mapCrossing(
        mapper: (Euclidean2DPosition) -> Euclidean2DPosition
    ): Euclidean2DCrossing =
        GraphEdgeWithData(
            tail.mapPolygon(mapper),
            head.mapPolygon(mapper),
            Pair(mapper.invoke(data.first), mapper.invoke(data.second))
        )

    private fun ConvexPolygon.mapPolygon(mapper: (Euclidean2DPosition) -> Euclidean2DPosition) =
        MutableConvexPolygonImpl(vertices().map(mapper).toMutableList())
}
