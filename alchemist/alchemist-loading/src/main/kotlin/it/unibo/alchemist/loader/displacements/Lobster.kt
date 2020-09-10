/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.displacements

import org.apache.commons.math3.random.MersenneTwister
import org.apache.commons.math3.random.RandomGenerator
import org.graphstream.algorithm.generator.BarabasiAlbertGenerator
import org.graphstream.algorithm.generator.LobsterGenerator
import org.graphstream.algorithm.generator.RandomEuclideanGenerator
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.stream.Sink
import org.graphstream.ui.layout.springbox.implementations.LinLog
import org.graphstream.ui.layout.springbox.implementations.SpringBox
import org.graphstream.util.VerboseSink
import java.lang.IllegalStateException
import java.util.Random

@ExperimentalUnsignedTypes
fun main() {
//    val random = MersenneTwister(2)
    System.setProperty("org.graphstream.ui", "swing")
    val nodes = 400
    val maxSize = 20
    val minSize = 7
    val graphs = (0..0).map {
        SingleGraph("asda").also { graph ->
            val layout = SpringBox(false, Random(1))
            with(LobsterGenerator(2, 10)) {
                addNodeLabels(false)
                setRandomSeed(0)
                addSink(graph)
                addSink(layout)
                layout.addSink(graph)
                layout.quality = 1.0
                begin()
                (0..nodes).forEach { _ ->
                    nextEvents()
                }
                end()
            }
            val degrees = graph.map { it.edges().count() }
            val maxDeg = degrees.maxOrNull() ?: 0
            val minDeg = degrees.minOrNull() ?: 0
            fun Long.scaled() = minSize + (this - minDeg) * (maxSize - minSize) / (maxDeg - minDeg)
            graph.forEach { it.setAttribute("ui.style", "size: ${it.edges().count().scaled()}px;") }
//            graph.display(false)
            var count = 0u
            while (layout.stabilization < 1) {
                layout.compute()
                count++
            }
            println(count)
            graph.forEach {
                val coordinates = it.getAttribute("xyz")
                if (coordinates is Array<*>) {
                    val actualCoordinates = coordinates.filterIsInstance<Double>()
                    println("${it.index} -> $actualCoordinates")
                    it.setAttribute("x", actualCoordinates[0])
                    it.setAttribute("y", actualCoordinates[1])
                } else {
                    throw IllegalStateException("Unexpected type ${coordinates::class}, an array was expected")
                }
            }
            graph.display(false)
        }
    }
    println(graphs)
}