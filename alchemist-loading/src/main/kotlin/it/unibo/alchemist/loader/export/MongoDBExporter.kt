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
 * TODO.
 * @param url TODO
 */
class MongoDBExporter<T, P : Position<P>>(val url: String) : AbstractExporter<T, P>() {

    override fun setupExportEnvironment(environment: Environment<T, P>?) {
        TODO("Not yet implemented")
    }

    override fun exportData(environment: Environment<T, P>?, reaction: Reaction<T>?, time: Time?, step: Long) {
        TODO("Not yet implemented")
    }

    override fun closeExportEnvironment(environment: Environment<T, P>?, time: Time?, step: Long) {
        TODO("Not yet implemented")
    }
}
