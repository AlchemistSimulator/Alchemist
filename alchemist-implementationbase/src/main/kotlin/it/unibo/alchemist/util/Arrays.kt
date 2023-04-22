/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * Container object for utility functions related to [Array]s.
 */
object Arrays {

    /**
     * Converts an array of numbers representing positions to an actual list of positions. E.g. the array [2,3,4,5] in
     * a bidimensional environment would be transformed into a list containing positions (2,3) and (4,5).
     */
    @JvmStatic
    fun <P : Position<P>> Array<out Number>.toPositions(environment: Environment<*, P>): List<P> =
        toList().chunked(environment.dimensions) { environment.makePosition(*it.toTypedArray()) }
}
