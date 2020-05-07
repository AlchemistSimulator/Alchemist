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
fun <E> List<E>.takePercentage(percentage: Double): List<E> = take((percentage * size).toInt())

/**
 * Converts an array of numbers representing [Euclidean2DPosition]s to an actual list of positions.
 */
fun Array<out Number>.toPositions(): List<Euclidean2DPosition> =
    toList().chunked(2) { Euclidean2DPosition(it[0].toDouble(), it[1].toDouble()) }

/**
 * Splits a sequence into a [Triple] of lists.
 * The first list contains elements for which [predicate1] yielded true,
 * the second list contains elements for which [predicate1] yielded false and [predicate2] yielded true,
 * the third list contains elements for which both predicates yielded false.
 */
fun <T> Sequence<T>.partition(
    predicate1: (T) -> Boolean,
    predicate2: (T) -> Boolean
): Triple<List<T>, List<T>, List<T>> = partition(predicate1).let { (first, toPartition) ->
    toPartition.partition(predicate2).let { (second, third) ->
        Triple(first, second, third)
    }
}
