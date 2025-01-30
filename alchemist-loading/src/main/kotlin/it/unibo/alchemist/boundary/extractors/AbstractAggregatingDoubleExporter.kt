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

/**
 * Aggregation of data extracted from the environment.
 * The data is filtered and then aggregated using the provided aggregators.
 * Provided a [filter] and a list of [aggregatorNames], extracts data from the environment,
 * filters it, and then aggregates it.
 */
abstract class AbstractAggregatingDoubleExporter
    @JvmOverloads
    constructor(
        private val filter: ExportFilter = CommonFilters.NOFILTER.filteringPolicy,
        aggregatorNames: List<String>,
        precision: Int? = null,
    ) : AbstractDoubleExporter(precision) {
        constructor(
            filter: String?,
            aggregatorNames: List<String>,
            precision: Int? = null,
        ) : this(
            filter = CommonFilters.fromString(filter),
            aggregatorNames = aggregatorNames,
            precision = null,
        )

        private companion object {
            private const val NAME = "aggregator"
        }

        private val aggregators: Map<String, UnivariateStatistic> =
            aggregatorNames
                .associateWith { StatUtil.makeUnivariateStatistic(it) }
                .filter { it.value.isPresent }
                .map { it.key to it.value.get() }
                .toMap()

        override val columnNames: List<String> =
            aggregators.keys
                .takeIf { it.isNotEmpty() }
                ?.map { "$NAME[$it]" }
                ?: listOf("$NAME@node-id")

        override fun <T> extractData(
            environment: Environment<T, *>,
            reaction: Actionable<T>?,
            time: Time,
            step: Long,
        ): Map<String, Double> {
            val filtered =
                extractDataAsText(environment, reaction, time, step)
                    .flatMap { filter.apply(it.value.toDouble()) }
                    .toDoubleArray()
            return aggregators
                .map { (aggregatorName, aggregator) ->
                    "$NAME[$aggregatorName]" to aggregator.evaluate(filtered)
                }.toMap()
        }
    }
