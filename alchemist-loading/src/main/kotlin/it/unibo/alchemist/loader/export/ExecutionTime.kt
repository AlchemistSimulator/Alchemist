/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export

import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time

class ExecutionTime<E> : Extractor<E>{

    companion object {
        private const val NANOS_TO_SEC: Double = 1e9
    }
    private val columnName: List<String> = listOf("runningTime")
    private var firstRun: Boolean = true
    private var initial: Long = 0L
    private var lastStep: Long = 0L


    override fun <T> extractData(
        environment: Environment<T, *>,
        reaction: Reaction<T>,
        time: Time,
        step: Long): Map<String, E> {
        if (lastStep > step) {
            firstRun = true
        }
        if (firstRun) {
            firstRun = false
            initial = System.nanoTime()
        }
        lastStep = step
        return mapOf<String, E>(columnName.get(0), ((System.nanoTime() - initial) / NANOS_TO_SEC))
    }

    override fun getColumnNames(): List<String> = columnName

    override val fixedColumnCount: Boolean = true
}