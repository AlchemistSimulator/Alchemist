/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export.exporters

import it.unibo.alchemist.boundary.exporters.AbstractExporter
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

/**
 * Save all available information in a static map.
 * @param interval the sampling time, defaults to [AbstractExporter.DEFAULT_INTERVAL].
 */
class MultiVestaExporter<T, P : Position<P>> @JvmOverloads constructor(
    val interval: Double = DEFAULT_INTERVAL,
) : AbstractExporter<T, P>(interval) {

    private val logger = LoggerFactory.getLogger(MultiVestaExporter::class.java)

    override fun setup(environment: Environment<T, P>) {
        values = ConcurrentHashMap()
    }

    override fun exportData(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
        logger.info("Exporting data for time $time")
        values = values + (
            environment.simulation to dataExtractors.flatMap { extractor ->
                extractor.extractData(environment, reaction, time, step).map { (dataLabel, dataValue) ->
                    dataLabel to dataValue
                }
            }.toMap()
            )
    }

    override fun close(environment: Environment<T, P>, time: Time, step: Long) {
        values = values - environment.simulation
    }

    /**
     * Utilities for the MultiVesta exporter.
     */
    companion object {
        private var values: Map<Simulation<*, *>, Map<String, Any>> = ConcurrentHashMap()

        /**
         * Get the value of the desired observation, if it exists.
         */
        fun getValue(simulation: Simulation<*, *>, observation: String): Any? =
            values[simulation]?.get(observation)

        /**
         * Get the value of the desired observation id, if it exists.
         */
        fun getValue(simulation: Simulation<*, *>, observationId: Int): Any? =
            values[simulation]?.entries?.elementAtOrNull(observationId)?.value
    }
}
