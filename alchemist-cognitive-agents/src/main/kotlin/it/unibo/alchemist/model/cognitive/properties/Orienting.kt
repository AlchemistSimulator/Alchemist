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
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.cognitive.OrientingProperty
import it.unibo.alchemist.model.euclidean.environments.EnvironmentWithGraph
import it.unibo.alchemist.model.euclidean.geometry.ConvexShape
import it.unibo.alchemist.model.euclidean.geometry.navigationgraph.NavigationGraph
import it.unibo.alchemist.model.euclidean.geometry.navigationgraph.UndirectedNavigationGraph
import it.unibo.alchemist.model.geometry.Transformation
import it.unibo.alchemist.model.geometry.Vector
import it.unibo.alchemist.model.interfaces.properties.OccupiesSpaceProperty
import it.unibo.alchemist.model.properties.AbstractNodeProperty
import it.unibo.alchemist.util.Iterables.shuffled
import it.unibo.alchemist.util.Lists.takeFraction
import org.apache.commons.math3.random.RandomGenerator
import org.jgrapht.Graph
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
          A : Transformation<P>,
          L : ConvexShape<P, A>,
          N : ConvexShape<P, A> {

    override val volatileMemory: MutableMap<ConvexShape<P, A>, Int> = HashMap()

    override val cognitiveMap: NavigationGraph<P, A, L, DefaultEdge> by lazy {
        val environmentGraph = environment.graph
        /*
         * The rooms in which landmarks will be placed.
         */
        val rooms = environmentGraph.vertexSet()
            .filter { it.diameter > node.asProperty<T, OccupiesSpaceProperty<T, P, A>>().shape.diameter * minArea }
            .shuffled(randomGenerator)
            .toList()
            .takeFraction(knowledgeDegree)
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

    companion object {

        /**
         * Checks whether a path exists between [source] and [sink].
         * [DijkstraShortestPath] is used instead of [org.jgrapht.alg.connectivity.ConnectivityInspector.pathExists],
         * because, in case of directed graph, the latter checks whether the given vertices lay in the same weakly
         * connected component, which is not the desired behavior.
         * As unweighted graphs have a default edge weight of 1.0, shortest path algorithms can always be applied
         * meaningfully.
         */
        fun <V> Graph<V, *>.pathExists(source: V, sink: V): Boolean =
            DijkstraShortestPath.findPathBetween(this, source, sink) != null
    }
}
