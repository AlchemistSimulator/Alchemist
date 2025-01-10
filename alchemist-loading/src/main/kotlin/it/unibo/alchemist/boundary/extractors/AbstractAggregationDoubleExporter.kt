/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.ExportFilter
import it.unibo.alchemist.boundary.exportfilters.CommonFilters
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.util.StatUtil
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic

abstract class AbstractAggregationDoubleExporter(
    private val filter: String?,
    aggregatorNames: List<String>,
    precision: Int? = null
) : AbstractDoubleExporter(precision) {
    private companion object {
        private const val NAME = "aggregator"
    }

    private val aggregators: Map<String, UnivariateStatistic> =
        aggregatorNames
            .associateWith { StatUtil.makeUnivariateStatistic(it) }
            .filter { it.value.isPresent }
            .map { it.key to it.value.get() }
            .toMap()

    private val singleColumnName: String = "$NAME"

    override val columnNames: List<String> =
        aggregators.keys
            .takeIf { it.isNotEmpty() }
            ?.map { "$singleColumnName[$it]" }
            ?: listOf("$singleColumnName@node-id")

    private fun associateFilter(filter: String): ExportFilter = CommonFilters.valueOf(filter).filteringPolicy //?: "Selected filter $filter is not valid."

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long
    ): Map<String, Double> {
        val actualFilter = associateFilter(filter.toString())
        val filtered = extractDataAsText(environment, reaction, time, step)
            .flatMap { actualFilter.apply(it.value.toDouble()) }
            .toDoubleArray()
        return aggregators.map { (aggregatorName, aggregator) ->
            "$singleColumnName[$aggregatorName]" to aggregator.evaluate(filtered)
        }.toMap()
//        val filtered = environment.nodes
//            .map { n -> environment.getNeighborhood(n).size() }
//            .flatMap { t -> actualFilter.apply(t.toDouble()) }
//            .toDoubleArray()
    }
}