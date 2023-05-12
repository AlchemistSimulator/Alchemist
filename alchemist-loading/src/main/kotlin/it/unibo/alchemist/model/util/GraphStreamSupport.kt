/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.util

import arrow.core.Either
import it.unibo.alchemist.model.Deployment
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.LinkingRule
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.linkingrules.OffsetGraphStreamLinkingRule
import it.unibo.alchemist.util.ClassPathScanner
import it.unimi.dsi.util.SplitMix64Random
import org.apache.commons.math3.util.MathArrays
import org.apache.commons.math3.util.MathArrays.ebeAdd
import org.apache.commons.math3.util.MathArrays.ebeDivide
import org.apache.commons.math3.util.MathArrays.ebeMultiply
import org.apache.commons.math3.util.MathArrays.ebeSubtract
import org.danilopianini.jirf.Factory
import org.danilopianini.jirf.FactoryBuilder
import org.graphstream.algorithm.generator.BaseGenerator
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.layout.springbox.implementations.SpringBox
import java.util.stream.Collectors
import kotlin.math.max
import kotlin.math.nextUp

/**
 * Support class for GraphStream, composed of a [linkingRule] and a [deployment].
 */
class GraphStreamSupport<T, P : Position<out P>>(
    val linkingRule: LinkingRule<T, P>,
    val deployment: Deployment<P>,
) {
    companion object {

        private val generators = ClassPathScanner.subTypesOf<BaseGenerator>("org.graphstream")

        private val factory: Factory = FactoryBuilder()
            .withAutoBoxing<Int>()
            .withAutomaticToString()
            .withArrayBoxing()
            .withWideningConversions()
            .withNarrowingConversions()
            .build()

        private fun generateGenerator(generatorName: String, vararg parameters: Any): BaseGenerator {
            val generatorClasses = findSuitableGeneratorsFor(generatorName)
            val parameterList = parameters.toList()
            val created = generatorClasses.asSequence()
                .map { synchronized(factory) { factory.build(it, parameterList) } }
                .map { construction ->
                    if (construction.createdObject.isPresent) {
                        Either.Right(construction.createdObject.get())
                    } else {
                        Either.Left(construction.exceptions)
                    }
                }
                .reduceOrNull { a, b ->
                    when {
                        a is Either.Left && b is Either.Left -> Either.Left(a.value + b.value)
                        a is Either.Right -> a
                        else -> b
                    }
                }
            return when (created) {
                is Either.Left -> throw created.value.values.reduce { a, b -> a.also { it.addSuppressed(b) } }
                is Either.Right -> created.value
                null ->
                    throw IllegalArgumentException(
                        "No suitable graph generator for name $generatorName," +
                            " try any of ${generators.map { it.simpleName }}",
                    )
            }
        }

        private fun findSuitableGeneratorsFor(generator: String) =
            with(generators) {
                val exactMatch = find {
                    it.simpleName == generator || it.simpleName == "${generator}Generator"
                }
                val match = when {
                    exactMatch != null -> listOf(exactMatch)
                    else ->
                        filter { it.simpleName.startsWith(generator, ignoreCase = true) }.takeUnless { it.isEmpty() }
                }
                match ?: throw IllegalArgumentException(
                    "None of the candidates in ${map { it.simpleName }} matches requested generator $generator",
                )
            }

        /**
         * Given an [environment], the [nodeCount] to be displaced,
         * the GraphStream's [generatorName] and the [parameters] for its constructor,
         * an identifier [uniqueId],
         * a [layoutQuality],
         * and possibly a flag to decide whether or not to compute z-dimensions [is3D].
         */
        @JvmOverloads
        fun <T, P : Position<P>> generateGraphStream(
            environment: Environment<T, P>,
            nodeCount: Int,
            offsetX: Double = 0.0,
            offsetY: Double = 0.0,
            offsetZ: Double = 0.0,
            zoom: Double = 1.0,
            generatorName: String = "EuclideanRandom",
            uniqueId: Long = 0,
            layoutQuality: Double = 1.0,
            is3D: Boolean = false,
            vararg parameters: Any,
        ): GraphStreamSupport<T, P> {
            val generator = generateGenerator(generatorName, *parameters)
            val randomGenerator = SplitMix64Random(uniqueId)
            val layout = SpringBox(is3D, randomGenerator)
            val graph = SingleGraph(generatorName)
            require(layoutQuality in 0.0..1.0) {
                "Invalid layout quality for graph generator $generatorName, must be in [0, 1]"
            }
            with(layout) {
                addSink(graph)
                quality = layoutQuality
            }
            with(generator) {
                addNodeLabels(false)
                setRandomSeed(randomGenerator.nextLong())
                addSink(layout)
                begin()
                // One node is inserted by GraphStream automatically
                while (graph.nodeCount < nodeCount) {
                    nextEvents()
                }
                end()
            }
            do { layout.compute() } while (layout.stabilization < max(layoutQuality, 0.0.nextUp()))
            val originalCoordinates = graph.nodes()
                .map { it.getAttribute("xyz") }
                .map { coordinates ->
                    require(coordinates is Array<*>) {
                        "Unexpected type '${coordinates?.let { it::class }}', an array was expected"
                    }
                    coordinates.map { (it as Number).toDouble() }
                }
                .map { coordinate -> coordinate.map { (it as Number).toDouble() }.toDoubleArray() }
                .collect(Collectors.toList())
            val sum = originalCoordinates.reduce(MathArrays::ebeAdd)
            val sizes = DoubleArray(sum.size) { graph.nodeCount.toDouble() }
            val barycenter = ebeDivide(sum, sizes)
            val zooms = DoubleArray(sum.size) { zoom }
            val offsets = doubleArrayOf(offsetX, offsetY, offsetZ)
            val shift = ebeAdd(barycenter, offsets)
            fun DoubleArray.zoomAndPan(): DoubleArray =
                ebeAdd(shift, ebeMultiply(zooms, ebeSubtract(this, barycenter)))
            return GraphStreamSupport(OffsetGraphStreamLinkingRule<T, P>(environment.nodeCount, graph)) {
                originalCoordinates.stream().map {
                    val shifted = it.zoomAndPan()
                    if (is3D) {
                        environment.makePosition(*shifted.toTypedArray())
                    } else {
                        environment.makePosition(shifted[0], shifted[1])
                    }
                }
            }
        }
    }
}
