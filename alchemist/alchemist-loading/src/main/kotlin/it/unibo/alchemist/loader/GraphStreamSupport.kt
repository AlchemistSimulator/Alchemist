/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader

import arrow.core.Either
import it.unibo.alchemist.ClassPathScanner
import it.unibo.alchemist.loader.displacements.Displacement
import it.unibo.alchemist.model.implementations.linkingrules.OffsetGraphStreamLinkingRule
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.LinkingRule
import it.unibo.alchemist.model.interfaces.Position
import it.unimi.dsi.util.SplitMix64Random
import org.danilopianini.jirf.Factory
import org.danilopianini.jirf.FactoryBuilder
import org.graphstream.algorithm.generator.BaseGenerator
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.layout.springbox.implementations.SpringBox

class GraphStreamSupport<T, P : Position<out P>>(
    val linkingRule: LinkingRule<T, P>,
    val displacement: Displacement<P>,
) {
    companion object {

        private val generators = ClassPathScanner
            .subTypesOf<BaseGenerator>("org.graphstream")

        private val factory: Factory = FactoryBuilder()
            .withAutoBoxing<Int>()
            .withAutomaticToString()
            .withArrayBoxing()
            .withWideningConversions()
            .withNarrowingConversions()
            .build()

        private fun generateGenerator(generatorName: String, vararg parameters: Any): BaseGenerator {
            val generatorClasses = with (generators) {
                val exactMatch = find {
                    it.simpleName == generatorName || it.simpleName == "${generatorName}Generator"
                }
                val match = when {
                    exactMatch != null -> listOf(exactMatch)
                    else -> filter { it.simpleName.startsWith(generatorName, ignoreCase = true) }
                        .takeUnless { it.isEmpty() }
                }
                match ?: throw IllegalArgumentException("None of the candidates in ${map { it.simpleName }}" +
                    " matches requested generator $generatorName")
            }
            var result: Either<() -> IllegalArgumentException, BaseGenerator> = Either.Left {
                IllegalArgumentException(
                    "Cannot create the requested generator $generatorName with parameters $parameters"
                )
            }
            val iterator = generatorClasses.iterator()
            while (result.isLeft() && iterator.hasNext()) {
                try {
                    result = Either.Right(factory.build(iterator.next(), parameters.toList()))
                } catch (e: IllegalArgumentException) {
                    val previousErrors = result as Either.Left
                    result = Either.left { previousErrors.a().apply { addSuppressed(e) } }
                }
            }
             when(result) {
                is Either.Right -> return result.b
                is Either.Left -> throw result.a()
            }
        }

        @JvmOverloads
        fun <T, P : Position<out P>> generateGraphStream(
            environment: Environment<T, P>,
            nodeCount: Int,
            generatorName: String = "EuclideanRandom",
            uniqueId: Long = 0,
            layoutQuality: Double = 1.0,
            is3D: Boolean = false,
            vararg parameters: Any
        ): GraphStreamSupport<T, P> {
            val generator = generateGenerator(generatorName, *parameters)
            val randomGenerator = SplitMix64Random(uniqueId)
            val layout = SpringBox(is3D, randomGenerator)
            val graph = SingleGraph(generatorName)
            with (layout) {
                addSink(graph)
                quality = layoutQuality.coerceIn(0.0, 1.0)
            }
            with(generator) {
                addNodeLabels(false)
                setRandomSeed(0)
                addSink(graph)
                addSink(layout)
                begin()
                repeat(nodeCount) { nextEvents() }
                end()
            }
            while (layout.stabilization < 1) {
                layout.compute()
            }
            return GraphStreamSupport(OffsetGraphStreamLinkingRule<T, P>(environment.nodeCount, graph)) {
                graph.nodes()
                    .map { it.getAttribute("xyz") }
                    .map {
                        if (it is Array<*>) {
                            it
                        } else {
                            throw IllegalStateException("Unexpected type ${it::class}, an array was expected")
                        }
                    }
                    .map { coordinate -> coordinate.map { it as Number } }
                    .map { (x, y, z) ->
                        if (is3D) {
                            environment.makePosition(x, y, z)
                        } else {
                            environment.makePosition(x, y)
                        }
                    }
            }
        }
    }
}
