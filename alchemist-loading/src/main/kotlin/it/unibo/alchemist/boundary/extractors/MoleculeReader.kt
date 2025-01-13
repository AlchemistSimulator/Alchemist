/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
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
import it.unibo.alchemist.model.Incarnation
import it.unibo.alchemist.model.Molecule
import it.unibo.alchemist.model.Node
import it.unibo.alchemist.model.Time
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
class MoleculeReader
    @JvmOverloads
    constructor(
        moleculeName: String,
        private val property: String?,
        private val incarnation: Incarnation<*, *>,
        private val filter: ExportFilter,
        aggregatorNames: List<String>,
        precision: Int? = null,
    ) : AbstractAggregatingDoubleExporter(filter, aggregatorNames, precision) {
        private companion object {
            private const val SHORT_NAME_MAX_LENGTH = 5
        }

        private val molecule: Molecule = incarnation.createMolecule(moleculeName)

        private val propertyText =
            if (property.isNullOrEmpty()) {
                ""
            } else {
                property.replace("[^\\d\\w]*".toRegex(), "")
            }

        private val shortProp =
            propertyText.takeIf(String::isEmpty)
                ?: "${propertyText.substring(0..<min(propertyText.length, SHORT_NAME_MAX_LENGTH))}@"

        private val singleColumnName: String = "$shortProp$moleculeName"

        override fun <T> extractData(
            environment: Environment<T, *>,
            reaction: Actionable<T>?,
            time: Time,
            step: Long,
        ): Map<String, Double> {
            fun Node<T>.extractData() = environment.incarnation.getProperty(this, molecule, property)
            return environment.nodes.associate { n -> "$singleColumnName@${n.id}" to n.extractData() }
        }
    }
