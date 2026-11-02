/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geogrid.utils

import java.util.Arrays

/*
 * Index-finding helpers over a regular geographic axis (the latitudes or longitudes of a RasterGrid).
 *
 * The shared precondition for every function is: axis must be non-empty and sorted in
 * STRICTLY ascending order with finite values (no duplicates, no NaN within the axis).
 *
 * The ascending order and the absence of Double.NaN within the axis are NOT verified, in order to
 * achieve a O(log(n)) time complexity.
 */

/**
 * Finds the pair of indices `(lowerIndex, upperIndex)` that bracket the given [coordinate]
 * on the [axis], such that `axis[lowerIndex] <= coordinate <= axis[upperIndex]`.
 *
 * The indices will be identical (`lowerIndex == upperIndex`) when the [coordinate] lands exactly
 * on a node, or when it is outside the axis boundaries (both indices clamp to the same edge).
 *
 * @param axis grid coordinates along one dimension, strictly ascending.
 * @param coordinate the query coordinate in the same unit as [axis].
 * @return a [Pair] where `first` is the lower index and `second` is the upper index.
 * @throws IllegalArgumentException if [axis] is empty or [coordinate] is NaN.
 */
internal fun bracketIndices(axis: DoubleArray, coordinate: Double): Pair<Int, Int> {
    require(axis.isNotEmpty()) { "Axis cannot be empty." }
    require(!coordinate.isNaN()) { "The query coordinate cannot be NaN." }
    val binarySearchResult = Arrays.binarySearch(axis, coordinate)
    val upperIndex = -binarySearchResult - 1
    return when {
        binarySearchResult >= 0 -> binarySearchResult to binarySearchResult
        upperIndex <= 0 -> 0 to 0
        upperIndex >= axis.size -> axis.lastIndex to axis.lastIndex
        else -> {
            val lowerIndex = upperIndex - 1
            lowerIndex to upperIndex
        }
    }
}

/**
 * Computes the normalized position of [coordinate] within the segment
 * `[axis[lowerIndex], axis[upperIndex]]`: `0.0` exactly at [lowerIndex], `1.0` exactly at
 * [upperIndex]. Returns `0.0` when `lowerIndex == upperIndex`.
 *
 * @param axis grid coordinates along one dimension, strictly ascending.
 * @param lowerIndex the lower boundary index, as returned by [bracketIndices].
 * @param upperIndex the upper boundary index, as returned by [bracketIndices].
 * @param coordinate the query coordinate, within `[axis[lowerIndex], axis[upperIndex]]`.
 * @return the interpolation weight in `[0.0, 1.0]`, or `0.0` if the indices are equal.
 */
internal fun weight(axis: DoubleArray, lowerIndex: Int, upperIndex: Int, coordinate: Double): Double {
    if (lowerIndex == upperIndex) return 0.0
    val totalSpan = axis[upperIndex] - axis[lowerIndex]
    val distanceFromLower = coordinate - axis[lowerIndex]
    return distanceFromLower / totalSpan
}
