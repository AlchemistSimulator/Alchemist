/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export.extractors

import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.export.FilteringPolicy
import it.unibo.alchemist.loader.export.StatUtil
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Actionable
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Node
import it.unibo.alchemist.model.interfaces.Time
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic
import kotlin.math.min

/**
 * Reads the value of a molecule and logs it.
 *
 * @param moleculeName
 *            the target molecule
 * @param property
 *            the target property
 * @param incarnation
 *            the target incarnation
 * @param filter
 *            the [FilteringPolicy] to use
 * @param aggregatorNames
 *            the names of the [UnivariateStatistic] to use for
 *            aggregating data. If an empty list is passed, then the values
 *            will be logged indipendently for each node.
 */
class MoleculeReader(
    moleculeName: String,
    private val property: String?,
    private val incarnation: Incarnation<*, *>,
    private val filter: FilteringPolicy,
    aggregatorNames: List<String>
) : Extractor<Double> {

    companion object {
        private const val SHORT_NAME_MAX_LENGTH = 5
    }

    private val molecule: Molecule = incarnation.createMolecule(moleculeName)

    private val aggregators: Map<String, UnivariateStatistic> = aggregatorNames
        .associateWith { StatUtil.makeUnivariateStatistic(it) }
        .filter { it.value.isPresent }
        .map { it.key to it.value.get() }
        .toMap()

    private val propertyText =
        if (property == null || property.isEmpty()) {
            ""
        } else {
            property.replace("[^\\d\\w]*".toRegex(), "")
        }

    private val shortProp = propertyText.takeIf(String::isEmpty)
        ?: "${propertyText.substring(0..min(propertyText.length, SHORT_NAME_MAX_LENGTH))}@"

    private val singleColumnName: String = "$shortProp$moleculeName"

    override val columnNames: List<String> = aggregators.keys.takeIf { it.isNotEmpty() }
        ?.map { "$singleColumnName[$it]" }
        ?: listOf("$singleColumnName@node-id")

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long
    ): Map<String, Double> {
        fun Node<T>.extractData() = environment.incarnation.getProperty(this, molecule, property)
        return when {
            aggregators.isEmpty() -> environment.nodes.asSequence().map { node ->
                "$singleColumnName@${node.id}" to node.extractData()
            }.toMap()
            else -> {
                val filtered = environment.nodes.flatMap { filter.apply(it.extractData()) }.toDoubleArray()
                aggregators.map { (aggregatorName, aggregator) ->
                    "$singleColumnName[$aggregatorName]" to aggregator.evaluate(filtered)
                }.toMap()
            }
        }
    }
}
