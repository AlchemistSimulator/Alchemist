/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time

/**
 * Exports the positions of all nodes in the environment.
 * The output is a table where, for each node, there is a column per dimension.
 *
 * Note: this exporter is not designed to handle changes in the environment topology like node removal or addition.
 */
class NodesPositions<T, P : Position<P>>(private val environment: Environment<T, P>) : AbstractDoubleExporter() {
    override val columnNames: List<String> by lazy {
        (0 until environment.nodeCount).flatMap { nodeId ->
            (0 until environment.dimensions).map { dimensionIndex ->
                columnNameFormat(nodeId, Dimension(dimensionIndex))
            }
        }
    }
    private val expectedNodesCount: Int by lazy { environment.nodeCount }
    private val maxNodeId: Int by lazy {
        environment.nodes.maxOfOrNull { it.id } ?: error("No nodes in the environment")
    }

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> {
        checkExtractCondition(environment)
        return environment.nodes
            .flatMap {
                val nodeId = it.id
                val nodePosition = environment.getPosition(it)
                nodePosition.coordinates.mapIndexed { index, coordinate ->
                    columnNameFormat(nodeId, Dimension(index)) to coordinate
                }
            }
            .toMap()
    }

    private fun <T> checkExtractCondition(environment: Environment<T, *>) {
        require(expectedNodesCount == environment.nodeCount) {
            "The number of nodes in the environment is ${environment.nodeCount}, but $expectedNodesCount was expected"
        }
        val currentMaxNodeId = environment.nodes.maxOfOrNull { it.id } ?: error("No nodes in the environment")
        require(maxNodeId == currentMaxNodeId) {
            """
                The maximum node ID in the environment is $currentMaxNodeId, but $maxNodeId was expected.
                
                This is likely due to a change in the environment topology like a node removal or addition.
                This exporter is not designed to handle such changes.
            """.trimIndent()
        }
    }

    companion object {

        @JvmInline
        private value class Dimension(val index: Int) {
            val symbol: String get() = "xyzwvu".getOrNull(index)?.toString() ?: "d$index"
        }

        private fun columnNameFormat(nodeIndex: Int, dimension: Dimension): String =
            "node-$nodeIndex-${dimension.symbol}"
    }
}
