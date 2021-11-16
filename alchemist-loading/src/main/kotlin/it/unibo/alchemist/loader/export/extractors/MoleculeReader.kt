/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
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
import it.unibo.alchemist.model.interfaces.Incarnation
import it.unibo.alchemist.model.interfaces.Molecule
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import org.apache.commons.math3.stat.descriptive.UnivariateStatistic

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
) : Extractor<Any> {

    companion object {
        private const val SHORT_NAME_MAX_LENGTH = 5
    }

    private val molecule: Molecule = incarnation.createMolecule(moleculeName)
    private val aggregators: Map<String, UnivariateStatistic> = aggregatorNames
        .associateWith { StatUtil.makeUnivariateStatistic(it) }
        .filter { it.value.isPresent }
        .map { it.key to it.value.get() }
        .toMap()

    private val propertyText = if (property == null || property.isEmpty()) ""
    else property.replace("[^\\d\\w]*".toRegex(), "")

    private val shortProp = if (propertyText.isEmpty()) ""
    else propertyText.substring(0, propertyText.length.coerceAtMost(SHORT_NAME_MAX_LENGTH)) + "@"

    private val singleColumnName: String = "$shortProp$moleculeName@every_node"
    private val columnNames: List<String> = if (aggregators.isEmpty()) {
        listOf(singleColumnName)
    } else {
        aggregators.map {
            shortProp + moleculeName + '[' + it.value::class.java.simpleName + ']'
        }.toList()
    }

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Reaction<T>?,
        time: Time,
        step: Long
    ): Map<String, Any> {
        val values: List<Double> = environment.nodes
            .map { node ->
                environment.incarnation.map {
                    it.getProperty(node, molecule, property)
                }
                    .orElseThrow { IllegalStateException("No incarnation available in the environment!") }
            }
        return if (aggregators.isEmpty()) mapOf(singleColumnName to values) else {
            values.forEach { filter.apply(it) }
            if (values.isEmpty()) aggregators.keys.associateWith { Double.NaN }
            else aggregators.entries.associate {
                it.key to it.value.evaluate(values.toDoubleArray())
            }
        }
    }

    override fun getColumnNames(): List<String> = columnNames
}
