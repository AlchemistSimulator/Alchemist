/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export

import it.unibo.alchemist.loader.variables.Variable
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time
import java.util.stream.Collectors

/**
 * Abstract implementation of a [Exporter].
 * @param samplingInterval the sampling time, defaults to [DEFAULT_INTERVAL].
 */
abstract class AbstractExporter<T, P : Position<P>> (
    private val samplingInterval: Double
) : Exporter<T, P> {

    override var dataExtractors: List<Extractor> = emptyList()

    /**
     * A value used to check if it's time to export data.
     * Starts with -1 because the 0th should be sampled.
     */
    private var count = -1L

    /**
     * A description of the [Variable]s of the current simulation and their values.
     */
    var variablesDescriptor: String = ""

    companion object {
        /**
         * If no sampling interval is specified, this option value is used. Defaults to 1.0.
         */
        const val DEFAULT_INTERVAL: Double = 1.0
    }

    override fun bindData(dataExtractors: List<Extractor>) {
        this.dataExtractors = dataExtractors
    }

    override fun bindVariables(variables: Map<String, Variable<*>>) {
        variablesDescriptor = variables.keys.stream().collect(Collectors.joining("-"))
    }

    /**
     *  Every step of the simulation check if is time to export data depending on the sampling interval.
     *  Converts the division of the current time and the interval to Long in order to export data only
     *  when the difference between steps is as big as the sampling interval.
     */
    override fun update(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
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
