/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.test

import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time

/**
 * Utility used to verify the correctness of CSVExport: when the columnNames matches the key set
 * returned by extract data, the values for each line should follow the columns name
 */
class ExtractorColumnAligned() : Extractor<Int> {
    override val columnNames = listOf("d", "c", "b", "a")
    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Reaction<T>?,
        time: Time,
        step: Long
    ): Map<String, Int> {
        val leftMap = columnNames.zip(columnNames.indices).toMap()
        return HashMap<String, Int>(leftMap)
    }
}
