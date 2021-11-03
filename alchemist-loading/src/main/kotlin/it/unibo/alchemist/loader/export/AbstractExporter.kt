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
import it.unibo.alchemist.model.interfaces.Position

/**
 * Abstract implementation of a [GenericExporter].
 */
abstract class AbstractExporter<T, P : Position<P>> : GenericExporter<T, P> {

    override var dataExtractor: List<Extractor> = emptyList()
    override var variables: Map<String, Variable<*>> = emptyMap()

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
        this.variables = variables
        variablesDescriptor = variables
            .map { (name, value) -> "$name-$value" }
            .joinToString(separator = "_")
    }
}
