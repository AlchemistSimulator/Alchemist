/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.actions

import it.unibo.alchemist.model.implementations.positions.Euclidean2DPosition

/**
 * Takes the first [percentage] * size elements of the list.
 */
fun <T> List<T>.takePercentage(percentage: Double): List<T> = take((percentage * size).toInt())

/**
 * Converts an array of numbers representing [Euclidean2DPosition]s to an actual list of positions.
 */
fun Array<out Number>.toPositions(): List<Euclidean2DPosition> =
    toList().chunked(2) { Euclidean2DPosition(it[0].toDouble(), it[1].toDouble()) }

/**
 * Performs the cartesian product of the given sequences.
 */
fun <T> cartesianProduct(sequence1: Sequence<T>, sequence2: Sequence<T>): Sequence<Pair<T, T>> =
    sequence1.flatMap { element1 -> sequence2.map { element2 -> Pair(element1, element2) } }
