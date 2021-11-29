/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export.exporters

import it.unibo.alchemist.loader.export.Exporter
import it.unibo.alchemist.loader.export.Extractor
import it.unibo.alchemist.loader.export.exporters.AbstractExporter.Companion.DEFAULT_INTERVAL
import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time

/**
 * Abstract implementation of a [Exporter].
 * @param samplingInterval the sampling time, defaults to [DEFAULT_INTERVAL].
 */
abstract class AbstractExporter<T, P : Position<P>> (
    private val samplingInterval: Double
) : Exporter<T, P> {

    final override lateinit var dataExtractors: List<Extractor<*>>
        private set

    /**
     * A description of the [Variable]s of the current simulation and their values.
     */
    protected lateinit var variablesDescriptor: String

    /**
     * A value used to check if it's time to export data.
     * Starts with -1 because the 0th should be sampled.
     */
    private var count = -1L

    companion object {
        /**
         * If no sampling interval is specified, this option value is used. Defaults to 1.0.
         */
        const val DEFAULT_INTERVAL: Double = 1.0
    }

    final override fun bindDataExtractors(dataExtractors: List<Extractor<*>>) {
        require(!this::dataExtractors.isInitialized) {
            "Re-binding data extractors is forbidden. Currently bound: ${this.dataExtractors}"
        }
        this.dataExtractors = dataExtractors
    }

    final override fun bindVariables(variables: Map<String, *>) {
        require(!this::variablesDescriptor.isInitialized) {
            "Re-binding variables is forbidden. Currently bound: $variablesDescriptor"
        }
        variablesDescriptor = computeDescriptor(variables)
    }

    protected fun computeDescriptor(variables: Map<String, *>): String =
        variables.entries.joinToString("_") { (name, value) -> "$name-$value" }

    /**
     *  Every step of the simulation check if is time to export data depending on the sampling interval.
     *  Converts the division of the current time and the interval to Long in order to export data only
     *  when the difference between steps is as big as the sampling interval.
     */
    final override fun update(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
        val curSample: Long = (time.toDouble() / samplingInterval).toLong()
        if (curSample > count) {
            count = curSample
            exportData(environment, reaction, time, step)
        }
    }

    /**
     * Delegates the concrete implementation of this method to his subclasses.
     */
    abstract fun exportData(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long)
}
