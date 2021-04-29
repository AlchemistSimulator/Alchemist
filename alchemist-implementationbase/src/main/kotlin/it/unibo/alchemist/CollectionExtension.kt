/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

inline fun <reified T> Collection<T>.meanOf(default: Double = 0.0, toDouble: T.() -> Double): Double =
    default.takeIf { isEmpty() } ?: (sumOf(toDouble) / size)
