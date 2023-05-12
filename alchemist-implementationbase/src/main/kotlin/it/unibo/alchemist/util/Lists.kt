/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.util

/**
 * Container object for utility functions related to [List]s.
 */
object Lists {

    /**
     * Takes the first [percentage] * size elements of the list.
     */
    @JvmStatic
    fun <T> List<T>.takeFraction(percentage: Double): List<T> = check(percentage in 0.0..1.0).run {
        take((percentage * size).toInt())
    }
}
