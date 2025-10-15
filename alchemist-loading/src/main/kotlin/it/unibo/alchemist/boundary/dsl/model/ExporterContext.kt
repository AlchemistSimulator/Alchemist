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
import it.unibo.alchemist.model.Position

class ExporterContext<T, P : Position<P>>(ctx: SimulationContext<T, P>) {
    var type: Exporter<T, P>? = null
    var extractors: List<Extractor<*>> = emptyList()

    fun data(vararg extractors: Extractor<*>) {
        this.extractors = extractors.toList()
    }
}
