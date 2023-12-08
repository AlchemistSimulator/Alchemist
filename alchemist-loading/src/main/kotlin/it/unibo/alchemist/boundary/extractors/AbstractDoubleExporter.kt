/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time
import java.util.Locale

/**
 * Double-formatting utility.
 * Provided a [precision] representing the significant digits, formats doubles accordingly, using [Locale.ENGLISH].
 * If `null` is provided, returns the default conversion to string.
 */
abstract class AbstractDoubleExporter @JvmOverloads constructor(val precision: Int? = null) : Extractor<Double> {

    init {
        require(precision == null || precision > 0) {
            "Significant digits must be positive"
        }
    }

    private val formatString = "%.${precision}g"

    /**
     * Uses this formatter to format some Double-encoded [data].
     */
    protected fun format(data: Double): String = precision?.run {
        String.format(Locale.ENGLISH, formatString, data)
    } ?: data.toString()

    final override fun <T> extractDataAsText(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, String> = extractData(environment, reaction, time, step).mapValues { format(it.value) }
}
