/*
 * Copyright (C) 2010-2021, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist

/**
 * Computes the mean of the values in a collection, provided a way to map the contents [toDouble].
 * If the collection is empty, returns 0.0.
 */
inline fun <reified T> Collection<T>.meanOrZero(toDouble: T.() -> Double): Double =
    meanOf(0.0, toDouble)

/**
 * Computes the mean of the values in a collection, provided a way to map the contents [toDouble] and a [default]
 * value to return in case of empty collection ([default] defaults to [Double.NaN]).
 */
inline fun <reified T> Collection<T>.meanOf(default: Double = Double.NaN, toDouble: T.() -> Double): Double =
    default.takeIf { isEmpty() } ?: (sumOf(toDouble) / size)
