/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.euclidean.twod.navigator

import it.unibo.alchemist.model.implementations.geometry.createSegment
import it.unibo.alchemist.model.implementations.geometry.intersection
import it.unibo.alchemist.model.implementations.geometry.isAxisAligned
import it.unibo.alchemist.model.implementations.geometry.isXAxisAligned
import it.unibo.alchemist.model.implementations.geometry.toInterval
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.navigator.ExtendableConvexPolygon
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.advanceEdge
import it.unibo.alchemist.model.implementations.geometry.euclidean.twod.edgeClosestTo
import it.unibo.alchemist.model.implementations.geometry.findExtremePoints
import it.unibo.alchemist.model.implementations.graph.Euclidean2DNavigationGraph
import it.unibo.alchemist.model.implementations.graph.Euclidean2DNavigationGraphBuilder
import it.unibo.alchemist.model.implementations.geometry.subtractAll
import it.unibo.alchemist.model.implementations.geometry.vertices
import it.unibo.alchemist.model.implementations.graph.Euclidean2DCrossing
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean.twod.Euclidean2DSegment
import it.unibo.alchemist.model.interfaces.graph.GraphEdgeWithData
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
 * - the generation of a navigation graph can take a significant amount of time.
 *
 * Here's a brief description of how the algorithm operates:
 * Firstly, a certain number of seeds is planted in the environment. Each seed is a
 * square-shaped region that will grow maintaining a convex shape. Secondly, planted
 * seeds are extended until possible (i.e. until they are in contact with an obstacle
 * or another seed on each side). Finally, crossings are found between the grown seeds.
 */
class NaviGator {
    
    /**
     * Generates an [Euclidean2DNavigationGraph] of a given environment.
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
     *              integer coordinates).
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
        val builder =
            Euclidean2DNavigationGraphBuilder()
        seeds.forEach { builder.addNode(it) }
        seeds
            .findCrossings(origin, width, height, obstacles, unity)
            .filter {
                !fuzzyEquals(it.data.first.getDistanceTo(it.data.second), 0.0)
            }
            .forEach { builder.addEdge(it) }
        return builder.build(destinations)
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

    private fun MutableList<out ExtendableConvexPolygon>.findCrossings(
        origin: Euclidean2DPosition,
        width: Double,
        height: Double,
        obstacles: Collection<Shape>,
        unity: Double
    ): Collection<Euclidean2DCrossing> {
        val crossings = mutableListOf<Euclidean2DCrossing>()
        forEach { seed ->
            seed.vertices().indices.forEach { i ->
                val edge = seed.getEdge(i)
                if (edge.isAxisAligned()) {
                    while (none { it != seed && it.intersects(seed.asAwtShape()) }) {
                        if (!seed.advanceEdge(i, unity, origin, width, height)) {
                            break
                        }
                    }
                    val intersectedSeeds = filter {
                        it != seed && it.intersects(seed.asAwtShape())
                    }
                    val intersectedObstacles = obstacles.filter { seed.intersects(it) }
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

    private fun createSeed(x: Double, y: Double, side: Double): ExtendableConvexPolygon =
        ExtendableConvexPolygonImpl(
            mutableListOf(
                Euclidean2DPosition(x, y),
                Euclidean2DPosition(x + side, y),
                Euclidean2DPosition(x + side, y + side),
                Euclidean2DPosition(x, y + side)))
}
