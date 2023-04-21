/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position

/**
 * Takes the first [percentage] * size elements of the list.
 */
fun <T> List<T>.takePercentage(percentage: Double): List<T> = take((percentage * size).toInt())

/**
 * Converts an array of numbers representing positions to an actual list of positions. E.g. the array [2,3,4,5] in
 * a bidimensional environment would be transformed into a list containing positions (2,3) and (4,5).
 */
fun <P : Position<P>> Array<out Number>.toPositions(environment: Environment<*, P>): List<P> =
    toList().chunked(environment.dimensions) { environment.makePosition(*it.toTypedArray()) }

/**
 * Performs the cartesian product of two sequences.
 */
fun <T> Sequence<T>.cartesianProduct(other: Sequence<T>): Sequence<Pair<T, T>> =
    flatMap { first -> other.map { second -> Pair(first, second) } }
