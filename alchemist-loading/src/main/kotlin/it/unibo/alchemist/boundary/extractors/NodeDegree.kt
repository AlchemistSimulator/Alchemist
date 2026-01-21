/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.ExportFilter
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time

/**
 * Extract the degree of the nodes in the network.
 * The degree of a node is the number of neighbors it has.
 * The result can be aggregated by the given [aggregators] and filtered by the given [filter].
 */
class NodeDegree
@JvmOverloads
constructor(filter: ExportFilter, aggregators: List<String>, precision: Int = 2) :
    AbstractAggregatingDoubleExtractor(filter, aggregators, precision) {
    override val columnName: String = NAME

    override fun <T> getData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<Node<T>, Double> = environment.nodes.associateWith { node ->
        environment.getNeighborhood(node).current.size().toDouble()
    }

    private companion object {
        private const val NAME = "nodes-degree"
    }
}
