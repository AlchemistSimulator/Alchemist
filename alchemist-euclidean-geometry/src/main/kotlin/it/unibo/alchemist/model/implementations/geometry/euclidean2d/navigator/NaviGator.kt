/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.euclidean2d.navigator

import it.unibo.alchemist.model.implementations.geometry.euclidean2d.AwtShapeExtension.vertices
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Segment2DImpl
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.graph.DirectedEuclidean2DNavigationGraph
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.Euclidean2DNavigationGraph
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.Euclidean2DPassage
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.navigator.ExtendableConvexPolygon
import it.unibo.alchemist.model.util.RangeExtension.findExtremeCoordsOnX
import it.unibo.alchemist.model.util.RangeExtension.findExtremeCoordsOnY
import it.unibo.alchemist.model.util.RangeExtension.intersect
import it.unibo.alchemist.model.util.RangeExtension.intersectsBoundsExcluded
import it.unibo.alchemist.model.util.RangeExtension.subtractAll
import org.danilopianini.lang.MathUtils.fuzzyEquals
import java.awt.Shape

/**
 * TODO(improve the quality of this algorithm)
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
 */
fun generateNavigationGraph(
    origin: Euclidean2DPosition = Euclidean2DPosition(0.0, 0.0),
    width: Double,
    height: Double,
    obstacles: List<Shape>,
    rooms: Collection<Euclidean2DPosition>,
    unity: Double = 1.0,
): Euclidean2DNavigationGraph {
    require(width > 0 && height > 0) { "width and height should be positive" }
    val seeds = rooms
        .map { createSeed(it.x, it.y, unity, origin, width, height, obstacles) }
        .toMutableList()
        .grow(obstacles, unity)
    val graph = DirectedEuclidean2DNavigationGraph(Euclidean2DPassage::class.java)
    seeds.forEach { graph.addVertex(it) }
    seeds.flatMap { seed ->
        seed.edges().mapIndexed { index, edge ->
            if (edge.isHorizontal || edge.isVertical) {
                val passages = seed.findPassages(index, seeds, origin, width, height, obstacles, unity)
                /*
                 * Moves the edge back to its previous position as findCrossings modified it.
                 */
                seed.replaceEdge(index, edge)
                passages
            } else {
                emptyList()
            }
        }.flatten()
    }.forEach { graph.addEdge(it.tail, it.head, it) }

    return graph
}

private fun MutableList<ExtendableConvexPolygonInEnvironment>.grow(
    obstacles: List<Shape>,
    step: Double,
): MutableList<ExtendableConvexPolygonInEnvironment> {
    removeIf { seed -> obstacles.any { seed.intersects(it) } }
    forEach { seed -> seed.polygonalObstacles = this - seed }
    var growing = true
    while (growing) {
        growing = false
        forEach { seed ->
            val extended = seed.extend(step)
            if (!growing) {
                growing = extended
            }
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
private fun ExtendableConvexPolygonInEnvironment.findPassages(
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
    remaining: ClosedRange<Double> = oldEdge.toRange(),
): Collection<Euclidean2DPassage> = emptyList<Euclidean2DPassage>()
    .takeIf { fuzzyEquals(remaining.start, remaining.endInclusive) }
    ?: let { _ ->
        /*
         * ToInterval functions map a shape or polygon to the DoubleInterval relevant for
         * the intersection with the advancing edge.
         */
        val polygonToInterval: (ExtendableConvexPolygon) -> ClosedRange<Double> = {
            it.closestEdgeTo(oldEdge).toRange(oldEdge.isHorizontal)
        }
        val shapeToInterval: (Shape) -> ClosedRange<Double> = { shape ->
            shape.vertices().let {
                if (oldEdge.isHorizontal) it.findExtremeCoordsOnX() else it.findExtremeCoordsOnY()
            }
        }
        val intersectedSeeds: () -> List<ExtendableConvexPolygon> = {
            seeds.filter {
                /*
                 * A seed is considered intersected if it intersects with the polygon and, in particular, with the
                 * remaining portion of the advancing edge. Similarly for obstacles below.
                 */
                it != this && it.intersects(asAwtShape()) && polygonToInterval(it).intersectsBoundsExcluded(remaining)
            }
        }
        val intersectedObstacles: () -> List<Shape> = {
            obstacles.filter {
                intersects(it) && shapeToInterval(it).intersectsBoundsExcluded(remaining)
            }
        }
        while (intersectedSeeds().isEmpty() && intersectedObstacles().isEmpty()) {
            if (!advanceEdge(index, unity)) {
                /*
                 * Edge is out of the environment's boundaries.
                 */
                return emptyList()
            }
        }
        val newRemaining = remaining.subtractAll(
            intersectedObstacles().map { shapeToInterval(it) },
        )
        val neighborToIntervals = intersectedSeeds().map { neighbor ->
            /*
             * Maps each neighbor to a collection of intervals representing
             * the portions of the advancing edge leading to that neighbor.
             */
            neighbor to newRemaining.mapNotNull { remaining ->
                polygonToInterval(neighbor).intersect(remaining)
            }
        }
        val passages = neighborToIntervals.flatMap { (neighbor, intervals) ->
            /*
             * Intervals should be mapped to actual segments, considering the
             * coordinate we ignored so far of the oldEdge.
             */
            intervals.map {
                val passageShape = when {
                    oldEdge.isHorizontal -> createSegment(it.start, oldEdge.first.y, x2 = it.endInclusive)
                    else -> createSegment(oldEdge.first.x, it.start, y2 = it.endInclusive)
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

private fun createSeed(
    x: Double,
    y: Double,
    side: Double,
    origin: Euclidean2DPosition,
    width: Double,
    height: Double,
    obstacles: List<Shape>,
): ExtendableConvexPolygonInEnvironment =
    ExtendableConvexPolygonInEnvironment(
        mutableListOf(
            Euclidean2DPosition(x, y),
            Euclidean2DPosition(x + side, y),
            Euclidean2DPosition(x + side, y + side),
            Euclidean2DPosition(x, y + side),
        ),
        origin,
        width,
        height,
        obstacles,
    )

/**
 * Creates a [Segment2D]. [x2] defaults to [x1] and [y2] defaults to [y1].
 */
private fun createSegment(x1: Double, y1: Double, x2: Double = x1, y2: Double = y1) =
    Segment2DImpl(Euclidean2DPosition(x1, y1), Euclidean2DPosition(x2, y2))
