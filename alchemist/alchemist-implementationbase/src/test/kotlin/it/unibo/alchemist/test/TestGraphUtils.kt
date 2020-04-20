/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.graph.builder.GraphBuilder
import it.unibo.alchemist.model.implementations.graph.builder.addEdge
import it.unibo.alchemist.model.implementations.graph.builder.addUndirectedEdge
import it.unibo.alchemist.model.implementations.graph.dijkstraShortestPath
import it.unibo.alchemist.model.implementations.graph.isReachable
import it.unibo.alchemist.model.implementations.graph.primMinimumSpanningForest
import it.unibo.alchemist.model.interfaces.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.graph.GraphEdgeWithData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests the [Graph] implementation.
 */
class TestGraphUtils {

    @Test
    fun testReachability() {
        val builder = GraphBuilder<Int, GraphEdge<Int>>()
        mutableListOf(0, 1, 2, 3).forEach { builder.addNode(it) }
        builder.addEdge(0, 1)
        builder.addEdge(0, 2)
        builder.addEdge(1, 2)
        builder.addEdge(2, 0)
        builder.addEdge(2, 3)
        val graph = builder.build()
        Assertions.assertEquals(true, graph.isReachable(1, 3))
        Assertions.assertEquals(false, graph.isReachable(3, 0))
    }

    @Test
    fun testShortestPath() {
        val builder = GraphBuilder<Int, GraphEdge<Int>>()
        mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9).forEach { builder.addNode(it) }
        builder.addEdge(1, 2)
        builder.addEdge(1, 3)
        builder.addEdge(1, 4)
        builder.addEdge(2, 3)
        builder.addEdge(2, 4)
        builder.addEdge(3, 4)
        builder.addEdge(3, 5)
        builder.addEdge(4, 6)
        builder.addEdge(5, 4)
        builder.addEdge(6, 5)
        builder.addEdge(7, 8)
        builder.addEdge(8, 9)
        builder.addEdge(9, 7)
        val graph = builder.build()
        Assertions.assertEquals(true, graph.isReachable(1, 6))
        Assertions.assertEquals(true, graph.isReachable(6, 5))
        Assertions.assertEquals(false, graph.isReachable(3, 2))
        Assertions.assertEquals(false, graph.isReachable(1, 7))
        Assertions.assertEquals(true, graph.isReachable(7, 9))
        var path = graph.dijkstraShortestPath(1, 6, { 1.0 })
        require(path != null) { "path shouldn't be null" }
        Assertions.assertEquals(mutableListOf(1, 4, 6), path.path)
        Assertions.assertEquals(null, graph.dijkstraShortestPath(1, 7, { 1.0 }))
        path = graph.dijkstraShortestPath(7, 9, { 1.0 })
        require(path != null) { "path shouldn't be null" }
        Assertions.assertEquals(mutableListOf(7, 8, 9), path.path)
        path = graph.dijkstraShortestPath(7, 7, { 1.0 })
        require(path != null) { "path shouldn't be null" }
        Assertions.assertEquals(mutableListOf(7), path.path)
    }

    @Test
    fun testMST() {
        val builder = GraphBuilder<Int, GraphEdgeWithData<Int, Int>>()
        mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8).forEach { builder.addNode(it) }
        builder.addUndirectedEdge(0, 1, 4)
        builder.addUndirectedEdge(0, 7, 8)
        builder.addUndirectedEdge(1, 2, 8)
        builder.addUndirectedEdge(1, 7, 11)
        builder.addUndirectedEdge(2, 8, 2)
        builder.addUndirectedEdge(2, 5, 4)
        builder.addUndirectedEdge(2, 3, 7)
        builder.addUndirectedEdge(3, 4, 9)
        builder.addUndirectedEdge(3, 5, 14)
        builder.addUndirectedEdge(4, 5, 10)
        builder.addUndirectedEdge(5, 6, 2)
        builder.addUndirectedEdge(6, 7, 1)
        builder.addUndirectedEdge(6, 8, 6)
        builder.addUndirectedEdge(7, 8, 7)
        val graph = builder.build()
        val mst = graph.primMinimumSpanningForest { e -> e.data.toDouble() }
        graph.nodes().forEach {
            Assertions.assertEquals(true, mst.nodes().contains(it))
        }
        Assertions.assertEquals(mutableSetOf(1, 7), mst.edgesFrom(0).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(0), mst.edgesFrom(1).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(3, 5, 8), mst.edgesFrom(2).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(2, 4), mst.edgesFrom(3).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(3), mst.edgesFrom(4).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(2, 6), mst.edgesFrom(5).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(5, 7), mst.edgesFrom(6).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(0, 6), mst.edgesFrom(7).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(2), mst.edgesFrom(8).map { it.head }.toSet())
    }

    @Test
    fun testMinimumSpanningForest() {
        val builder = GraphBuilder<Int, GraphEdgeWithData<Int, Int>>()
        mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).forEach { builder.addNode(it) }
        builder.addUndirectedEdge(0, 1, 4)
        builder.addUndirectedEdge(0, 7, 8)
        builder.addUndirectedEdge(1, 2, 8)
        builder.addUndirectedEdge(1, 7, 11)
        builder.addUndirectedEdge(2, 8, 2)
        builder.addUndirectedEdge(2, 5, 4)
        builder.addUndirectedEdge(2, 3, 7)
        builder.addUndirectedEdge(3, 4, 9)
        builder.addUndirectedEdge(3, 5, 14)
        builder.addUndirectedEdge(4, 5, 10)
        builder.addUndirectedEdge(5, 6, 2)
        builder.addUndirectedEdge(6, 7, 1)
        builder.addUndirectedEdge(6, 8, 6)
        builder.addUndirectedEdge(7, 8, 7)
        // disconnected part below
        builder.addUndirectedEdge(9, 10, 1)
        builder.addUndirectedEdge(9, 12, 3)
        builder.addUndirectedEdge(9, 11, 4)
        builder.addUndirectedEdge(10, 12, 2)
        builder.addUndirectedEdge(11, 12, 5)
        val graph = builder.build()
        val mst = graph.primMinimumSpanningForest { e -> e.data.toDouble() }
        graph.nodes().forEach {
            Assertions.assertEquals(true, mst.nodes().contains(it))
        }
        Assertions.assertEquals(mutableSetOf(1, 7), mst.edgesFrom(0).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(0), mst.edgesFrom(1).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(3, 5, 8), mst.edgesFrom(2).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(2, 4), mst.edgesFrom(3).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(3), mst.edgesFrom(4).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(2, 6), mst.edgesFrom(5).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(5, 7), mst.edgesFrom(6).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(0, 6), mst.edgesFrom(7).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(2), mst.edgesFrom(8).map { it.head }.toSet())
        // disconnected part below
        Assertions.assertEquals(mutableSetOf(11, 10), mst.edgesFrom(9).map { it.head }.toSet())
        Assertions.assertEquals(mutableSetOf(9, 12), mst.edgesFrom(10).map { it.head }.toSet())
    }
}
