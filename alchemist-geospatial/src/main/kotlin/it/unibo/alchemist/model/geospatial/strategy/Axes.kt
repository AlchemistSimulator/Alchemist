/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geospatial.strategy

import java.util.Arrays

/*
 * Index-finding helpers over a regular geographic axis (the latitudes or longitudes of a RasterGrid).
 *
 * The shared precondition for every function is: axis must be non-empty and sorted in
 * STRICTLY ascending order with finite values (no duplicates, no NaN within the axis).
 *
 * The ascending order and the absence of NaN within the axis are NOT verified: an O(n) scan
 * would defeat the purpose of the O(log n) search, so violating them yields undefined results.
 * An empty axis and a NaN query coordinate throw an IllegalArgumentException, as those checks
 * are O(1).
 */

/**
 * Finds the index of the closest coordinate on the [axis] relative to the query [coordinate].
 *
 * If the [coordinate] lies outside the boundaries of the axis, the result is clamped to the edge
 * (`0` if below the first node, `axis.lastIndex` if above the last node). This makes the function
 * suitable for both nearest-neighbor sampling inside the grid and edge-clamping outside it.
 * Ties (when the coordinate is exactly halfway between two nodes) resolve to the lower index.
 *
 * @param axis grid coordinates along one dimension, strictly ascending.
 * @param coordinate the query coordinate in the same unit as [axis].
 * @return a valid index into [axis], guaranteed to be within `0..axis.lastIndex`.
 * @throws IllegalArgumentException if [axis] is empty or [coordinate] is NaN.
 */
internal fun nearestIndex(axis: DoubleArray, coordinate: Double): Int {
    require(axis.isNotEmpty()) { "Axis cannot be empty." }
    require(!coordinate.isNaN()) { "The query coordinate cannot be NaN." }

    val binarySearchResult = Arrays.binarySearch(axis, coordinate)

    // exact match found: the coordinate aligns perfectly with a grid node.
    if (binarySearchResult >= 0) return binarySearchResult

    /*
     * binarySearch returns -(insertionPoint) - 1 when no match is found. Inverting the formula
     * yields the insertion point: the index of the first node strictly greater than the coordinate.
     */
    val upperIndex = -binarySearchResult - 1

    if (upperIndex <= 0) return 0
    if (upperIndex >= axis.size) return axis.lastIndex

    // the coordinate is between axis[lowerIndex] and axis[upperIndex]
    val lowerIndex = upperIndex - 1

    val distanceToLower = coordinate - axis[lowerIndex]
    val distanceToUpper = axis[upperIndex] - coordinate

    // return the index with the smaller distance. Ties go to lowerIndex.
    return if (distanceToLower <= distanceToUpper) lowerIndex else upperIndex
}

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

    // exact match
    if (binarySearchResult >= 0) return binarySearchResult to binarySearchResult

    val upperIndex = -binarySearchResult - 1

    if (upperIndex <= 0) return 0 to 0
    if (upperIndex >= axis.size) return axis.lastIndex to axis.lastIndex

    val lowerIndex = upperIndex - 1
    return lowerIndex to upperIndex
}

/**
 * Computes the normalized position of [coordinate] within the segment
 * `[axis[lowerIndex], axis[upperIndex]]`: `0.0` exactly at [lowerIndex], `1.0` exactly at
 * [upperIndex]. Returns `0.0` when `lowerIndex == upperIndex`, avoiding a
 * division by a zero span.
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
