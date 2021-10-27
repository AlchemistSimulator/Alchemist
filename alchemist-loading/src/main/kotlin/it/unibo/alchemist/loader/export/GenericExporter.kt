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
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time

/**
 * A generic exporter for the simulation.
 */
interface GenericExporter<T, P : Position<P>> {

    /**
     *  The List of [Extractor] used to export simulations data.
     */
    var dataExtractor: List<Extractor>

    /**
     * Assign the list of [Extractor] to the selected [GenericExporter].
     */
    fun bindData(dataExtractor: List<Extractor>)

    /**
     *  Prepare the export environment before the simulation starts.
     */
    fun setupExportEnvironment(environment: Environment<T, P>?)

    /**
     * Main method used by exporters to export data.
     */
    fun exportData(environment: Environment<T, P>?, reaction: Reaction<T>?, time: Time?, step: Long)

    /**
     * Used by the [GenericExporter] to stop the export in a correct way.
     */
    fun closeExportEnvironment(environment: Environment<T, P>?, time: Time?, step: Long)
}
