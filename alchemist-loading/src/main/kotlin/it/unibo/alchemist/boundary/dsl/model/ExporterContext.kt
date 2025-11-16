/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.model

import it.unibo.alchemist.boundary.Exporter
import it.unibo.alchemist.boundary.Extractor
import it.unibo.alchemist.boundary.dsl.AlchemistDsl
import it.unibo.alchemist.model.Position

/**
 * Context interface for configuring data exporters in a simulation.
 *
 * Exporters define how simulation data is extracted and exported, supporting formats
 * such as CSV, MongoDB, and custom formats.
 * Data can be exported per-node or aggregated
 * using statistical functions.
 *
 * ## Usage Example
 *
 * ```kotlin
*     exporter {
*         type = CSVExporter("output", 4.0)
*         data(Time(), moleculeReader("moleculeName"))
*     }
 * ```
 *
 * @param T The type of molecule concentration.
 * @param P The type of position, must extend [Position].
 *
 * @see [SimulationContext.exporter] for adding exporters to a simulation
 * @see [Exporter] for the exporter interface
 * @see [Extractor] for data extraction
 */
@AlchemistDsl
interface ExporterContext<T, P : Position<P>> {

    /** The parent simulation context. */
    val ctx: SimulationContext<T, P>

    /**
     * The exporter instance that handles data output.
     *
     * @see [Exporter]
     */
    var type: Exporter<T, P>?

    /**
     * Sets the data extractors for this exporter.
     *
     * Extractors define which data should be exported from the simulation.
     *
     * ```kotlin
     * data(Time(), moleculeReader("moleculeName"))
     * ```
     *
     * @param extractors The extractors to use for data extraction.
     * @see [Extractor]
     */
    fun data(vararg extractors: Extractor<*>)
}
