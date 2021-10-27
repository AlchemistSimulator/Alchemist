/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export

import it.unibo.alchemist.boundary.interfaces.OutputMonitor
import it.unibo.alchemist.model.interfaces.Environment
import it.unibo.alchemist.model.interfaces.Position
import it.unibo.alchemist.model.interfaces.Reaction
import it.unibo.alchemist.model.interfaces.Time

/**
 *  The intermediate between exporters and OutputMonitor interface.
 *  @param exporters TODO
 */
class GlobalExporter<T, P : Position<P>> (private val exporters: List<GenericExporter<T, P>>) : OutputMonitor<T, P> {

    override fun initialized(environment: Environment<T, P>?) {
        exporters.forEach() {
            it.setupExportEnvironment(environment)
        }
    }

    override fun stepDone(environment: Environment<T, P>?, reaction: Reaction<T>?, time: Time?, step: Long) {
        exporters.forEach() {
            it.exportData(environment, reaction, time, step)
        }
    }

    override fun finished(environment: Environment<T, P>?, time: Time?, step: Long) {
        exporters.forEach() {
            it.closeExportEnvironment(environment, time, step)
        }
    }
}
