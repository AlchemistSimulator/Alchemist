/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export.exporters

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.loader.export.Exporter
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.GlobalReaction
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Time

/**
 *  Contains all exporters selected in the configuration file.
 *  Implements the [OutputMonitor] interface and delegate the export phase to each one of his internal exporters.
 *  @param exporters The list of [Exporter].
 */
class GlobalExporter<T, P : Position<P>>(
    private val exporters: List<Exporter<T, P>>
) : OutputMonitor<T, P> {

    @Override
    override fun initialized(environment: Environment<T, P>) {
        exporters.forEach {
            it.setup(environment)
        }
    }

    override fun stepDone(environment: Environment<T, P>, reaction: GlobalReaction<T>?, time: Time, step: Long) {
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
