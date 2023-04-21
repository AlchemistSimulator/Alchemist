/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.properties

import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Node.Companion.asProperty
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.geometry.GeometricTransformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.implementations.actions.takePercentage
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.graph.UndirectedNavigationGraph
import it.unibo.alchemist.model.implementations.geometry.euclidean2d.graph.pathExists
import it.unibo.alchemist.model.interfaces.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.interfaces.geometry.ConvexGeometricShape
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.graph.NavigationGraph
import it.unibo.alchemist.model.interfaces.properties.OccupiesSpaceProperty
import it.unibo.alchemist.model.interfaces.properties.OrientingProperty
import it.unibo.alchemist.model.util.IterableExtension.shuffled
import org.apache.commons.math3.random.RandomGenerator
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree
import org.jgrapht.graph.AsWeightedGraph
import org.jgrapht.graph.DefaultEdge

/**
 * Base implementation of a node's [OrientingProperty].
 */
abstract class Orienting<T, P, A, N, L> @JvmOverloads constructor(
    /**
     * The simulation [RandomGenerator].
     */
    val randomGenerator: RandomGenerator,
    override val environment: EnvironmentWithGraph<*, T, P, A, N, DefaultEdge>,
    override val node: Node<T>,
    override val knowledgeDegree: Double,
    /**
     * Environment's areas whose diameter is smaller than ([minArea] * the diameter of this pedestrian) will be
     * regarded as too small and discarded when generating the cognitive map (i.e. no landmark will be placed inside
     * them).
     */
    private val minArea: Double = 10.0,
) : AbstractNodeProperty<T>(node), OrientingProperty<T, P, A, L, N, DefaultEdge>
    where P : Position<P>,
          P : Vector<P>,
          A : GeometricTransformation<P>,
          L : ConvexGeometricShape<P, A>,
          N : ConvexGeometricShape<P, A> {

    override val volatileMemory: MutableMap<ConvexGeometricShape<P, A>, Int> = HashMap()

    override val cognitiveMap: NavigationGraph<P, A, L, DefaultEdge> by lazy {
        val environmentGraph = environment.graph
        /*
         * The rooms in which landmarks will be placed.
         */
        val rooms = environmentGraph.vertexSet()
            .filter { it.diameter > node.asProperty<T, OccupiesSpaceProperty<T, P, A>>().shape.diameter * minArea }
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
            fullGraph.edgeSet() - PrimMinimumSpanningTree(fullGraphWeighted).spanningTree.edges,
        )
        fullGraph
    }

    /**
     * Creates a landmark entirely contained in the given area. If such area contains one or more destinations, the
     * returned landmark must contain at least one of them.
     */
    abstract override fun createLandmarkIn(area: N): L

    override fun toString() = "${super.toString()}[knoledgeDegree=$knowledgeDegree]"
}
