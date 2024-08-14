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
import it.unibo.alchemist.model.Time

/**
 * Exports the positions of all nodes in the environment, assuming [nodesCount] nodes.
 * The output is a table where for each node there are two columns, one for the x coordinate and one for the y coordinate.
 */
class NodesPositions(private val nodesCount: Int) : AbstractDoubleExporter() {
    override val columnNames: List<String> = (0 until nodesCount)
        .flatMap { listOf(columnNameFormat(it, "x"), columnNameFormat(it, "y")) }

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> {
        require(nodesCount == environment.nodeCount) {
            "The number of nodes in the environment is ${environment.nodeCount}, but $nodesCount was expected"
        }
        return environment.nodes
            .flatMap {
                val nodeId = it.id
                val nodePosition = environment.getPosition(it)
                listOf(
                    columnNameFormat(nodeId, "x") to nodePosition.getCoordinate(0),
                    columnNameFormat(nodeId, "y") to nodePosition.getCoordinate(1),
                )
            }
            .toMap()
    }

    companion object {
        private fun columnNameFormat(nodeIndex: Int, coordinateName: String): String = "node-$nodeIndex@$coordinateName"
    }
}
