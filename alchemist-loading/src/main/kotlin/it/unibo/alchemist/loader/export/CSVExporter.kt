/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.loader.export

import it.unibo.alchemist.model.interfaces.Position

/**
 * Writes on file data provided by a number of {@link Extractor}s. Produces a
 * CSV with '#' as comment character.e
 * @param filename TODO
 */
class CSVExporter<T, P : Position<P>>(val filename: String) : GenericExporter<T, P> {

    override fun setupExportEnvironment() {
        TODO("Not yet implemented")
    }

    override fun exportData() {
        TODO("Not yet implemented")
    }

    override fun closeExportEnvironment() {
        TODO("Not yet implemented")
    }
}
