/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.capabilities

import it.unibo.alchemist.model.implementations.actions.takePercentage
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.Ellipse
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.graph.UndirectedNavigationGraph
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.graph.pathExists
import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.capabilities.OrientingCapability
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.GeometricShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.GeometricTransformation
import it.unibo.alchemist.model.interfaces.geometry.Vector
import it.unibo.alchemist.shuffled
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree
import org.jgrapht.graph.AsWeightedGraph
import org.jgrapht.graph.DefaultEdge
import it.unibo.alchemist.model.interfaces.Node.Companion.asCapability
import it.unibo.alchemist.model.interfaces.capabilities.Spatial2DCapability
import it.unibo.alchemist.model.interfaces.capabilities.SpatialCapability
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.ConvexPolygon
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DShapeFactory
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Euclidean2DTransformation
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.NavigationGraph
import it.unibo.alchemist.nextDouble
import org.apache.commons.math3.random.RandomGenerator
import java.awt.geom.Ellipse2D
import java.awt.geom.Rectangle2D

private typealias Position2D = Euclidean2DPosition
private typealias Transformation2D = Euclidean2DTransformation
private typealias ShapeFactory = Euclidean2DShapeFactory

/**
 * Base implementation of a node's [OrientingCapability].
 */
abstract class BaseOrientingCapability<T, P, A, N, L, F> @JvmOverloads constructor(
    /**
     * The simulation [RandomGenerator].
     */
    open val randomGenerator: RandomGenerator,
    override val node: Node<T>,
    override val knowledgeDegree: Double,
    /**
     * Environment's areas whose diameter is smaller than ([minArea] * the diameter of this pedestrian) will be
     * regarded as too small and discarded when generating the cognitive map (i.e. no landmark will be placed inside
     * them).
     */
    private val minArea: Double = 10.0,
) : OrientingCapability<T, P, A, L, N, DefaultEdge, F>
    where P : Position<P>,
          P : Vector<P>,
          A : GeometricTransformation<P>,
          L : ConvexGeometricShape<P, A>,
          N : ConvexGeometricShape<P, A>,
          F : GeometricShapeFactory<P, A> {

    override val volatileMemory: MutableMap<ConvexGeometricShape<P, A>, Int> = HashMap()

    override fun cognitiveMap(
        environment: EnvironmentWithGraph<*, T, P, A, N, DefaultEdge>,
        knowledgeDegree: Double
    ): NavigationGraph<P, A, L, DefaultEdge> {
        val environmentGraph = environment.graph
        /*
         * The rooms in which landmarks will be placed.
         */
        val rooms = environmentGraph.vertexSet()
            .filter { it.diameter > node.asCapability<T, SpatialCapability<T, P, A>>().shape.diameter * minArea }
            .shuffled(randomGenerator)
            .toList()
            .takePercentage(knowledgeDegree)
            .toMutableList()
        /*
         * landmarks[i] will contain the landmark generated in rooms[i].
         */
        val landmarks = rooms.map { createLandmarkIn(it) }
        val fullGraph = UndirectedNavigationGraph<P, A, L, DefaultEdge>(DefaultEdge::class.java)
        landmarks.forEach { fullGraph.addVertex(it) }
        rooms.indices.forEach { i ->
            rooms.indices.forEach { j ->
                if (i != j && environmentGraph.pathExists(rooms[i], rooms[j])) {
                    fullGraph.addEdge(landmarks[i], landmarks[j])
                }
            }
        }
        /*
         * The environment's graph is unweighted, but edges' weights defaults to 1.0
         */
        val dijkstra = DijkstraShortestPath(environmentGraph)
        val weightFunction: (DefaultEdge) -> Double = { edge ->
            val tail = fullGraph.getEdgeSource(edge)
            val head = fullGraph.getEdgeTarget(edge)
            /*
             * The weight of the shortest path between two rooms (tail, head) is the number
             * of rooms that need to be traversed to go from tail to head.
             */
            dijkstra.getPathWeight(rooms[landmarks.indexOf(tail)], rooms[landmarks.indexOf(head)])
        }
        val fullGraphWeighted = AsWeightedGraph(fullGraph, weightFunction, false, false)
        /*
         * Only the edges in the spanning tree are maintained.
         */
        fullGraph.removeAllEdges(
            fullGraph.edgeSet() - PrimMinimumSpanningTree(fullGraphWeighted).spanningTree.edges
        )
        return fullGraph
    }

    /**
     * Creates a landmark entirely contained in the given area. If such area contains one or more destinations, the
     * returned landmark must contain at least one of them.
     */
    abstract override fun createLandmarkIn(area: N): L
}

/**
 * Basic implementation of a node's [OrientingCapability] in a 2D space.
 */
class BaseOrienting2DCapability<T, N : ConvexPolygon, E>(
    override val randomGenerator: RandomGenerator,
    override val node: Node<T>,
    override val knowledgeDegree: Double,
    /**
     * The starting width and height of the generated Ellipses will be a random quantity in
     * ([minSide, maxSide] * the diameter of this pedestrian).
     */
    private val minSide: Double = 30.0,
    private val maxSide: Double = 60.0,
) : BaseOrientingCapability<T, Position2D, Transformation2D, N, Ellipse, ShapeFactory>(
    randomGenerator,
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
            node.asCapability<T, Spatial2DCapability<T, Position2D, Transformation2D>>().shape.diameter
}
