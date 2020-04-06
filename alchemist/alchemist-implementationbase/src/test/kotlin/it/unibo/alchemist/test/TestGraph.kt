/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.graph.pathExists
import org.jgrapht.Graph
import org.jgrapht.alg.shortestpath.DijkstraShortestPath
import org.jgrapht.alg.spanning.PrimMinimumSpanningTree
import org.jgrapht.graph.AsWeightedGraph
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.DefaultUndirectedWeightedGraph
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * TODO: to be removed.
 *
 * This class tests the [org.jgrapht] library, which is not our business. It is maintained
 * in the first phases of the migration from our own graph implementation to that library,
 * just in order to find any unwanted or unexpected behavior more rapidly.
 */
class TestGraph {

    @Test
    fun testReachability() {
        val graph = DefaultDirectedGraph<Int, DefaultEdge>(DefaultEdge::class.java)
        mutableListOf(0, 1, 2, 3).forEach { graph.addVertex(it) }
        graph.addEdge(0, 1)
        graph.addEdge(0, 2)
        graph.addEdge(1, 2)
        graph.addEdge(2, 0)
        graph.addEdge(2, 3)
        Assertions.assertEquals(true, graph.pathExists(1, 3))
        Assertions.assertEquals(false, graph.pathExists(3, 0))
    }

    /*
     * Assumes edges of unitary weight.
     */
    private fun <V, E> Graph<V, E>.dijkstraShortestPath(source: V, target: V) =
        AsWeightedGraph(this, edgeSet().map { it to 1.0 }.toMap()).let { weighted ->
            DijkstraShortestPath(weighted).getPath(source, target)
        }

    @Test
    fun testShortestPath() {
        val graph = DefaultDirectedGraph<Int, DefaultEdge>(DefaultEdge::class.java)
        mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9).forEach { graph.addVertex(it) }
        graph.addEdge(1, 2)
        graph.addEdge(1, 3)
        graph.addEdge(1, 4)
        graph.addEdge(2, 3)
        graph.addEdge(2, 4)
        graph.addEdge(3, 4)
        graph.addEdge(3, 5)
        graph.addEdge(4, 6)
        graph.addEdge(5, 4)
        graph.addEdge(6, 5)
        graph.addEdge(7, 8)
        graph.addEdge(8, 9)
        graph.addEdge(9, 7)
        Assertions.assertEquals(true, graph.pathExists(1, 6))
        Assertions.assertEquals(true, graph.pathExists(6, 5))
        Assertions.assertEquals(false, graph.pathExists(3, 2))
        Assertions.assertEquals(false, graph.pathExists(1, 7))
        Assertions.assertEquals(true, graph.pathExists(7, 9))
        var path = graph.dijkstraShortestPath(1, 6)
        require(path != null) { "path shouldn't be null" }
        Assertions.assertEquals(mutableListOf(1, 4, 6), path.vertexList)
        Assertions.assertEquals(null, graph.dijkstraShortestPath(1, 7))
        path = graph.dijkstraShortestPath(7, 9)
        require(path != null) { "path shouldn't be null" }
        Assertions.assertEquals(mutableListOf(7, 8, 9), path.vertexList)
        path = graph.dijkstraShortestPath(7, 7)
        require(path != null) { "path shouldn't be null" }
        Assertions.assertEquals(mutableListOf(7), path.vertexList)
        Assertions.assertEquals(0.0, path.weight)
    }

    private fun <V, E> Graph<V, E>.addWeightedEdge(source: V, target: V, weight: Int) =
        setEdgeWeight(addEdge(source, target), weight.toDouble())

    private fun <V, E> Graph<V, E>.mst(): Graph<V, E> {
        val mst = DefaultUndirectedWeightedGraph(vertexSupplier, edgeSupplier)
        vertexSet().forEach { mst.addVertex(it) }
        PrimMinimumSpanningTree(this).spanningTree.edges.forEach { e ->
            val tail = getEdgeSource(e)
            val head = getEdgeTarget(e)
            mst.addEdge(tail, head, e)
        }
        return mst
    }

    private fun <V, E> Graph<V, E>.neighbors(node: V): Set<V> =
        outgoingEdgesOf(node).map { edge ->
            getEdgeTarget(edge).takeIf { it != node } ?: getEdgeSource(edge)
        }.toSet()

    @Test
    fun testMinimumSpanningForest() {
        val graph = DefaultUndirectedWeightedGraph<Int, DefaultEdge>(DefaultEdge::class.java)
        mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).forEach { graph.addVertex(it) }
        graph.addWeightedEdge(0, 1, 4)
        graph.addWeightedEdge(0, 7, 8)
        graph.addWeightedEdge(1, 2, 8)
        graph.addWeightedEdge(1, 7, 11)
        graph.addWeightedEdge(2, 8, 2)
        graph.addWeightedEdge(2, 5, 4)
        graph.addWeightedEdge(2, 3, 7)
        graph.addWeightedEdge(3, 4, 9)
        graph.addWeightedEdge(3, 5, 14)
        graph.addWeightedEdge(4, 5, 10)
        graph.addWeightedEdge(5, 6, 2)
        graph.addWeightedEdge(6, 7, 1)
        graph.addWeightedEdge(6, 8, 6)
        graph.addWeightedEdge(7, 8, 7)
        // disconnected part below
        graph.addWeightedEdge(9, 10, 1)
        graph.addWeightedEdge(9, 12, 3)
        graph.addWeightedEdge(9, 11, 4)
        graph.addWeightedEdge(10, 12, 2)
        graph.addWeightedEdge(11, 12, 5)
        val mst = graph.mst()
        Assertions.assertEquals(mutableSetOf(1, 7), mst.neighbors(0))
        Assertions.assertEquals(mutableSetOf(0), mst.neighbors(1))
        Assertions.assertEquals(mutableSetOf(3, 5, 8), mst.neighbors(2))
        Assertions.assertEquals(mutableSetOf(2, 4), mst.neighbors(3))
        Assertions.assertEquals(mutableSetOf(3), mst.neighbors(4))
        Assertions.assertEquals(mutableSetOf(2, 6), mst.neighbors(5))
        Assertions.assertEquals(mutableSetOf(5, 7), mst.neighbors(6))
        Assertions.assertEquals(mutableSetOf(0, 6), mst.neighbors(7))
        Assertions.assertEquals(mutableSetOf(2), mst.neighbors(8))
        // disconnected part below
        Assertions.assertEquals(mutableSetOf(11, 10), mst.neighbors(9))
        Assertions.assertEquals(mutableSetOf(9, 12), mst.neighbors(10))
    }
}
