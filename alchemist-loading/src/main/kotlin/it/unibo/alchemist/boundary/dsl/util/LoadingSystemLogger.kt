/*
 * Copyright (C) 2010-2025, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.dsl.util

import it.unibo.alchemist.boundary.dsl.SingleUseDslLoader
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal object LoadingSystemLogger {
    val logger: Logger = LoggerFactory.getLogger(SingleUseDslLoader::class.java)
}
