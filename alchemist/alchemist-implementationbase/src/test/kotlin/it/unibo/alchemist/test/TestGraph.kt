/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.model.implementations.geometry.graph.*
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdge
import it.unibo.alchemist.model.interfaces.geometry.graph.GraphEdgeWithData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class TestGraph {

    @Test
    fun testReachability() {
        val adjacencyList: LinkedHashMap<Int, MutableList<GraphEdge<Int>>> = LinkedHashMap()
        mutableListOf(0, 1, 2, 3).forEach { adjacencyList[it] = mutableListOf() }
        adjacencyList[0]!!.add(GraphEdge(0, 1))
        adjacencyList[0]!!.add(GraphEdge(0, 2))
        adjacencyList[1]!!.add(GraphEdge(1, 2))
        adjacencyList[2]!!.add(GraphEdge(2, 0))
        adjacencyList[2]!!.add(GraphEdge(2, 3))
        val g = GraphImpl(adjacencyList)
        Assertions.assertEquals(true, g.isReachable(1, 3))
        Assertions.assertEquals(false, g.isReachable(3, 0))
    }

    @Test
    fun testShortestPath() {
        val adjacencyList: LinkedHashMap<Int, MutableList<GraphEdge<Int>>> = LinkedHashMap()
        mutableListOf(1, 2, 3, 4, 5, 6, 7, 8, 9).forEach { adjacencyList[it] = mutableListOf() }
        adjacencyList[1]!!.add(GraphEdge(1, 2))
        adjacencyList[1]!!.add(GraphEdge(1, 3))
        adjacencyList[1]!!.add(GraphEdge(1, 4))
        adjacencyList[2]!!.add(GraphEdge(2, 3))
        adjacencyList[2]!!.add(GraphEdge(2, 4))
        adjacencyList[3]!!.add(GraphEdge(3, 4))
        adjacencyList[3]!!.add(GraphEdge(3, 5))
        adjacencyList[4]!!.add(GraphEdge(4, 6))
        adjacencyList[5]!!.add(GraphEdge(5, 4))
        adjacencyList[6]!!.add(GraphEdge(6, 5))
        adjacencyList[7]!!.add(GraphEdge(7, 8))
        adjacencyList[8]!!.add(GraphEdge(8, 9))
        adjacencyList[9]!!.add(GraphEdge(9, 7))
        val g = GraphImpl(adjacencyList)
        Assertions.assertEquals(true, g.isReachable(1, 6))
        Assertions.assertEquals(true, g.isReachable(6, 5))
        Assertions.assertEquals(false, g.isReachable(3, 2))
        Assertions.assertEquals(false, g.isReachable(1, 7))
        Assertions.assertEquals(true, g.isReachable(7, 9))
        Assertions.assertEquals(mutableListOf(1, 4, 6), g.dijkstraShortestPath(1, 6, { 1.0 })!!.path)
        Assertions.assertEquals(null, g.dijkstraShortestPath(1, 7, { 1.0 }))
        Assertions.assertEquals(mutableListOf(7, 8, 9), g.dijkstraShortestPath(7, 9, { 1.0 })!!.path)
        Assertions.assertEquals(mutableListOf(7), g.dijkstraShortestPath(7, 7, { 1.0 })!!.path)
    }

    @Test
    fun testMST() {
        val adjacencyList: LinkedHashMap<Int, MutableList<GraphEdgeWithData<Int, Int>>> = LinkedHashMap()
        mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8).forEach { adjacencyList[it] = mutableListOf() }
        adjacencyList.addUndirectEdge(0, 1, 4)
        adjacencyList.addUndirectEdge(0, 7, 8)
        adjacencyList.addUndirectEdge(1, 2, 8)
        adjacencyList.addUndirectEdge(1, 7, 11)
        adjacencyList.addUndirectEdge(2, 8, 2)
        adjacencyList.addUndirectEdge(2, 5, 4)
        adjacencyList.addUndirectEdge(2, 3, 7)
        adjacencyList.addUndirectEdge(3, 4, 9)
        adjacencyList.addUndirectEdge(3, 5, 14)
        adjacencyList.addUndirectEdge(4, 5, 10)
        adjacencyList.addUndirectEdge(5, 6, 2)
        adjacencyList.addUndirectEdge(6, 7, 1)
        adjacencyList.addUndirectEdge(6, 8, 6)
        adjacencyList.addUndirectEdge(7, 8, 7)
        val g = GraphImpl(adjacencyList)
        val mst = g.primMST { e -> e.data.toDouble() }
        g.nodes().forEach {
            Assertions.assertEquals(true, mst.nodes().contains(it))
        }
        Assertions.assertEquals(mutableSetOf(1, 7), mst.edgesFrom(0).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(0), mst.edgesFrom(1).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(3, 5, 8), mst.edgesFrom(2).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(2, 4), mst.edgesFrom(3).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(3), mst.edgesFrom(4).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(2, 6), mst.edgesFrom(5).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(5, 7), mst.edgesFrom(6).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(0, 6), mst.edgesFrom(7).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(2), mst.edgesFrom(8).map { it.to }.toSet())
    }

    @Test
    fun testMinimumSpanningForest() {
        val adjacencyList: LinkedHashMap<Int, MutableList<GraphEdgeWithData<Int, Int>>> = LinkedHashMap()
        mutableListOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12).forEach { adjacencyList[it] = mutableListOf() }
        adjacencyList.addUndirectEdge(0, 1, 4)
        adjacencyList.addUndirectEdge(0, 7, 8)
        adjacencyList.addUndirectEdge(1, 2, 8)
        adjacencyList.addUndirectEdge(1, 7, 11)
        adjacencyList.addUndirectEdge(2, 8, 2)
        adjacencyList.addUndirectEdge(2, 5, 4)
        adjacencyList.addUndirectEdge(2, 3, 7)
        adjacencyList.addUndirectEdge(3, 4, 9)
        adjacencyList.addUndirectEdge(3, 5, 14)
        adjacencyList.addUndirectEdge(4, 5, 10)
        adjacencyList.addUndirectEdge(5, 6, 2)
        adjacencyList.addUndirectEdge(6, 7, 1)
        adjacencyList.addUndirectEdge(6, 8, 6)
        adjacencyList.addUndirectEdge(7, 8, 7)
        // disconnected part below
        adjacencyList.addUndirectEdge(9, 10, 1)
        adjacencyList.addUndirectEdge(9, 12, 3)
        adjacencyList.addUndirectEdge(9, 11, 4)
        adjacencyList.addUndirectEdge(10, 12, 2)
        adjacencyList.addUndirectEdge(11, 12, 5)
        val g = GraphImpl(adjacencyList)
        val mst = g.primMST { e -> e.data.toDouble() }
        g.nodes().forEach {
            Assertions.assertEquals(true, mst.nodes().contains(it))
        }
        Assertions.assertEquals(mutableSetOf(1, 7), mst.edgesFrom(0).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(0), mst.edgesFrom(1).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(3, 5, 8), mst.edgesFrom(2).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(2, 4), mst.edgesFrom(3).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(3), mst.edgesFrom(4).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(2, 6), mst.edgesFrom(5).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(5, 7), mst.edgesFrom(6).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(0, 6), mst.edgesFrom(7).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(2), mst.edgesFrom(8).map { it.to }.toSet())
        // disconnected part below
        Assertions.assertEquals(mutableSetOf(11, 10), mst.edgesFrom(9).map { it.to }.toSet())
        Assertions.assertEquals(mutableSetOf(9, 12), mst.edgesFrom(10).map { it.to }.toSet())
    }

    private fun <N, D> HashMap<N, MutableList<GraphEdgeWithData<N, D>>>.addUndirectEdge(from: N, to: N, data: D) {
        this[from]!!.add(GraphEdgeWithData(from, to, data))
        this[to]!!.add(GraphEdgeWithData(to, from, data))
    }
}
