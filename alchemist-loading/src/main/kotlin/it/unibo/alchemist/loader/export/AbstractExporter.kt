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
 * Abstract implementation of a [GenericExporter].
 * @param samplingInterval the sampling time, defaults to [DEFAULT_INTERVAL].
 */
abstract class AbstractExporter<T, P : Position<P>> (
    private val samplingInterval: Double
) : GenericExporter<T, P> {

    override var dataExtractor: List<Extractor> = emptyList()

    /**
     * The 0th should be sampled.
     */
    var count = -1L
    /**
     * A description of the [Variable]s of the current simulation and their values.
     */
    lateinit var variablesDescriptor: String

    companion object {
        /**
         * If no sampling interval is specified, this option value is used. Defaults to 1.0.
         */
        const val DEFAULT_INTERVAL: Double = 1.0
    }

    override fun bindData(dataExtractor: List<Extractor>) {
        this.dataExtractor = dataExtractor
    }

    override fun bindVariables(variables: Map<String, Variable<*>>) {
        variablesDescriptor = variables.keys.stream().collect(Collectors.joining("-"))
    }

    override fun processData(environment: Environment<T, P>, reaction: Reaction<T>?, time: Time, step: Long) {
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
