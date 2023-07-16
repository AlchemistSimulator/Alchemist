/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.extractors

import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Time

/**
 * An extractor which provides informations about the running time of the simulation.
 * Optionally, a [precision] (significant digits) can be provided.
 */
class ExecutionTime @JvmOverloads constructor(
    precision: Int? = null,
) : AbstractDoubleExporter(precision) {

    companion object {
        private const val NANOS_TO_SEC: Double = 1e9
    }
    private val colName: String = "runningTime"
    private var firstRun: Boolean = true
    private var initial: Long = 0L
    private var lastStep: Long = 0L

    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Actionable<T>?,
        time: Time,
        step: Long,
    ): Map<String, Double> {
        if (lastStep > step) {
            firstRun = true
        }
        if (firstRun) {
            firstRun = false
            initial = System.nanoTime()
        }
        lastStep = step
        return mapOf(colName to ((System.nanoTime() - initial) / NANOS_TO_SEC))
    }

    override val columnNames = listOf(colName)
}
