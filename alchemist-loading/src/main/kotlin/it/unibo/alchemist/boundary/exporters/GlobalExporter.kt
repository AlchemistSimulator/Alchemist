/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.exporters

import it.unibo.alchemist.boundary.Exporter
import it.unibo.alchemist.boundary.OutputMonitor
import it.unibo.alchemist.model.Actionable
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import it.unibo.alchemist.model.Time

/**
 * Aggregates and delegates to multiple exporters selected in the configuration file.
 * Implements the [OutputMonitor] interface and delegates the export phase to each internal exporter.
 *
 * @param T the concentration type
 * @param P the position type
 * @property exporters the list of exporters to delegate to
 */
class GlobalExporter<T, P : Position<P>>(val exporters: List<Exporter<T, P>>) : OutputMonitor<T, P> {
    @Override
    override fun initialized(environment: Environment<T, P>) {
        exporters.forEach {
            it.setup(environment)
        }
    }

    override fun stepDone(environment: Environment<T, P>, reaction: Actionable<T>?, time: Time, step: Long) {
        exporters.forEach {
            it.update(environment, reaction, time, step)
        }
    }

    @Override
    override fun finished(environment: Environment<T, P>, time: Time, step: Long) {
        exporters.forEach {
            it.close(environment, time, step)
        }
    }
}
