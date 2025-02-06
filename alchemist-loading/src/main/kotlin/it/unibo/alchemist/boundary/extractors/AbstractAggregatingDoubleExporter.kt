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
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
import it.unibo.alchemist.util.StatUtil
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic

/**
 * Aggregation of data extracted from the environment.
 * The data is filtered and then aggregated using the provided aggregators.
 * Provided a [filter] and a list of [aggregatorNames] and a [name], extracts data from the environment,
 * filters it, and then aggregates it.
 * Available aggregators are: AbstractStorelessUnivariateStatistic, AbstractUnivariateStatistic, GeometricMean,
 * Kurtosis, Max, Mean, Median, Min, Percentile, Product, PSquarePercentile, SecondMoment, SemiVariance,
 * Skewness, StandardDeviation, Sum, SumOfLogs, SumOfSquares, Variance.
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

        /**
         * The name of the column in the output file.
         */
        abstract val columnName: String

        private val aggregators: Map<String, UnivariateStatistic> =
            aggregatorNames.associateWith {
                StatUtil.makeUnivariateStatistic(it).orElseThrow {
                    IllegalArgumentException(
                        "Unknown statistic $it. Available statistics are: ${StatUtil.availableStatistics()}",
                    )
                }
            }

        override val columnNames: List<String> by lazy {
            aggregators.keys
                .takeIf { it.isNotEmpty() }
                ?.map { "$columnName[$it]" }
                ?: listOf("$columnName@node-id")
        }

        final override fun <T> extractData(
            environment: Environment<T, *>,
            reaction: Actionable<T>?,
            time: Time,
            step: Long,
        ): Map<String, Double> =
            when {
                aggregators.isEmpty() ->
                    getData(environment, reaction, time, step)
                        .mapKeys { (key, _) -> "$columnName@${key.id}" }
                else -> {
                    val data =
                        getData(environment, reaction, time, step)
                            .values
                            .flatMap { filter.apply(it) }
                            .toDoubleArray()
                    aggregators
                        .map { (aggregator, statistics) ->
                            "$columnName[$aggregator]" to statistics.evaluate(data)
                        }.toMap()
                }
            }

        /**
         * Delegated to the concrete implementation to extract data from the environment.
         */
        abstract fun <T> getData(
            environment: Environment<T, *>,
            reaction: Actionable<T>?,
            time: Time,
            step: Long,
        ): Map<Node<T>, Double>
    }
