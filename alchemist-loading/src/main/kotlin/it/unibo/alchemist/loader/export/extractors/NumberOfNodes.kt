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
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time

/**
 * Logs the number of nodes in the scenario.
 */
class NumberOfNodes : Extractor<Int> {

    private val colName: String = "nodes"

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Reaction<T>?,
        time: Time,
        step: Long
    ): Map<String, Int> = mapOf(colName to environment.nodeCount)

    override val columnNames = listOf(colName)
}
