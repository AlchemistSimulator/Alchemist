/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.euclidean.twod.navigator

import it.unibo.alchemist.model.implementations.geometry.DoubleInterval
import it.unibo.alchemist.model.implementations.geometry.DoubleInterval.Companion.findExtremePoints
import it.unibo.alchemist.model.implementations.geometry.DoubleInterval.Companion.toInterval
import it.unibo.alchemist.model.implementations.geometry.createSegment
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.navigator.ExtendableConvexPolygon
import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.graph.DirectedEuclidean2DNavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Segment2D
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DPassage
import it.unibo.alchemist.model.interfaces.graph.Euclidean2DNavigationGraph
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.awt.Shape

/**
 * NaviGator (Navigation Graphs Generator) is an algorithm capable of generating an
 * [Euclidean2DNavigationGraph] of a given environment with obstacles. The nodes of
 * the produced graph are convex polygons representing the areas of the environment
 * traversable by agents (namely, walkable areas), whereas edges represent connections
 * between them.
 *
 * NaviGator works with rectangular-shaped bidimensional environments with euclidean
 * geometry and double precision coordinates. Note that this algorithm:
 * - does not guarantee the coverage of 100% of the walkable area.
 * - is only capable to deal with convex polygonal obstacles (concave ones can be
 * decomposed into convex meshes, whereas for curves bounding boxes can be used).
 * - is only capable to detect axis-aligned crossings.
 * - can take a significant amount of time to generate a navigation graph.
 *
 * Here's a brief description of how the algorithm operates:
 * Firstly, a certain number of seeds is planted in the environment. Each seed is a
 * square-shaped region of unitary side that will grow maintaining a convex shape.
 * Secondly, planted seeds are extended until possible (i.e. until they are in contact
 * with an obstacle or another seed on each side). Finally, crossings are found between
 * the grown seeds.
 *
 * @param origin
 *              the origin of the environment, defaults to (0,0).
 * @param width
 *              the width of the environment (only positive).
 * @param height
 *              the height of the environment (only positive).
 * @param obstacles
 *              the obstacles of the environment (only convex polygonal obstacles
 *              are supported).
 * @param rooms
 *              a collection of positions where to plant initial seeds. In indoor
 *              environments, these positions are usually located inside rooms
 *              (and corridors), hence the name of the parameter.
 * @param unity
 *              the quantity considered to be a unit in the environment (defaults
 *              to 1.0 because this algorithm works best with environments featuring
 *              integer coordinates). In the growing phase, each side of each seed
 *              will be advanced of a quantity equal to unity iteratively, hence the
 *              smaller this value is the slower the algorithm will be.
 * @param destinations
 *              a collection of positions of interest that will be stored in the
 *              navigation graph and may be used during navigation (e.g. destinations
 *              in an evacuation scenario).
 */
fun generateNavigationGraph(
    origin: Euclidean2DPosition = Euclidean2DPosition(0.0, 0.0),
    width: Double,
    height: Double,
    obstacles: Collection<Shape>,
    rooms: Collection<Euclidean2DPosition>,
    unity: Double = 1.0,
    destinations: List<Euclidean2DPosition>
): Euclidean2DNavigationGraph {
    require(width > 0 && height > 0) { "width and height should be positive" }
    val seeds = rooms
        .map { createSeed(it.x, it.y, unity) }
        .toMutableList()
        .grow(origin, width, height, obstacles, unity)
    val graph = DirectedEuclidean2DNavigationGraph(destinations, Euclidean2DPassage::class.java)
    seeds.forEach { graph.addVertex(it) }
    seeds.flatMap { seed ->
        seed.edges().mapIndexed { index, edge ->
            if (edge.isAxisAligned) {
                val passages = seed.findPassages(index, seeds, origin, width, height, obstacles, unity)
                /*
                 * Moves the edge back to its previous position as findCrossings modified it.
                 */
                seed.moveEdge(index, edge)
                passages
            } else emptyList()
        }.flatten()
    }.forEach { graph.addEdge(it.tail, it.head, it) }
    return graph
}

private fun MutableList<ExtendableConvexPolygon>.grow(
    origin: Euclidean2DPosition,
    width: Double,
    height: Double,
    envObstacles: Collection<Shape>,
    step: Double
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
            val extended = seed.extend(step, obstacles, origin, width, height)
            if (!growing) {
                growing = extended
            }
            obstacles.add(envObstacles.size + i, seed.asAwtShape())
        }
    }
    return this
}

/*
 * Finds the passages on the side of the polygon specified by the index parameter.
 * The specified side should be axis-aligned. This method is recursive and modifies
 * the specified edge.
 * In brief, this method advances the specified edge, keeping track of the portions
 * of it not occluded by obstacles yet, until every passage has been detected.
 */
private fun ExtendableConvexPolygon.findPassages(
    index: Int,
    seeds: Collection<ExtendableConvexPolygon>,
    origin: Euclidean2DPosition,
    width: Double,
    height: Double,
    obstacles: Collection<Shape>,
    unity: Double,
    /*
     * Original position of the edge being advanced.
     */
    oldEdge: Segment2D<Euclidean2DPosition> = getEdge(index),
    /*
     * Portion of the advancing edge not occluded by obstacles yet. Since the edge
     * is axis-aligned, a DoubleInterval is sufficient to represent a portion of it.
     */
    remaining: DoubleInterval = oldEdge.toInterval()
): Collection<Euclidean2DPassage> = emptyList<Euclidean2DPassage>()
    .takeIf { fuzzyEquals(remaining.first, remaining.second) }
    ?: let {
        /*
         * ToInterval functions map a shape or polygon to the DoubleInterval relevant for
         * the intersection with the advancing edge.
         */
        val polygonToInterval: (ExtendableConvexPolygon) -> DoubleInterval = {
            it.closestEdgeTo(oldEdge).toInterval(oldEdge.xAxisAligned)
        }
        val shapeToInterval: (Shape) -> DoubleInterval = {
            it.vertices().findExtremePoints(oldEdge.xAxisAligned)
        }
        val intersectedSeeds: () -> List<ExtendableConvexPolygon> = {
            seeds.filter {
                it != this && it.intersects(asAwtShape()) &&
                    /*
                     * A seed is considered intersected if it intersects with the polygon
                     * and, in particular, with the remaining portion of the advancing edge.
                     * Similarly for obstacles below.
                     */
                    polygonToInterval(it).intersectsEndpointsExcluded(remaining)
            }
        }
        val intersectedObstacles: () -> List<Shape> = {
            obstacles.filter {
                intersects(it) && shapeToInterval(it).intersectsEndpointsExcluded(remaining)
            }
        }
        while (intersectedSeeds().isEmpty() && intersectedObstacles().isEmpty()) {
            if (!advanceEdge(index, unity, origin, width, height)) {
                /*
                 * Edge is out of the environment's boundaries.
                 */
                return emptyList()
            }
        }
        val newRemaining = remaining.subtractAll(
            intersectedObstacles().map { shapeToInterval(it) }
        )
        val neighborToIntervals = intersectedSeeds().map { neighbor ->
            /*
             * Maps each neighbor to a collection of intervals representing
             * the portions of the advancing edge leading to that neighbor.
             */
            neighbor to newRemaining.mapNotNull { remaining ->
                polygonToInterval(neighbor).intersectionEndpointsExcluded(remaining)
            }
        }
        val passages = neighborToIntervals.flatMap { (neighbor, intervals) ->
            /*
             * Intervals should be mapped to actual segments, considering the
             * coordinate we ignored so far of the oldEdge.
             */
            intervals.map {
                val passageShape = when {
                    oldEdge.xAxisAligned -> createSegment(it.first, oldEdge.first.y, x2 = it.second)
                    else -> createSegment(oldEdge.first.x, it.first, y2 = it.second)
                }
                Euclidean2DPassage(this, neighbor, passageShape)
            }
        }
        return passages + newRemaining.flatMap {
            /*
             * The portions of edge that became passages won't be considered further.
             */
            it.subtractAll(neighborToIntervals.flatMap { (_, intervals) -> intervals })
        }.flatMap {
            findPassages(index, seeds, origin, width, height, obstacles, unity, oldEdge, it)
        }
    }

private fun createSeed(x: Double, y: Double, side: Double): ExtendableConvexPolygon =
    ExtendableConvexPolygonImpl(
        mutableListOf(
            Euclidean2DPosition(x, y),
            Euclidean2DPosition(x + side, y),
            Euclidean2DPosition(x + side, y + side),
            Euclidean2DPosition(x, y + side)))
