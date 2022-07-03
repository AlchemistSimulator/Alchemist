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
import it.unibo.alchemist.model.interfaces.Actionable
import it.unibo.alchemist.model.interfaces.Time

/**
 * An extractor which provides informations about the running time of the simulation.
 *
 */
class ExecutionTime : Extractor<Double> {

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
        step: Long
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
