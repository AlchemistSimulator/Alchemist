/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.interfaces.geometry.euclidean2d

import it.unibo.alchemist.model.geometry.Vector2D

/**
 * Describes the result an intersection operation in an euclidean 2D space.
 * Type [V] must extend [Vector2D].
 * The requirement is not explicitly enforced to allow the class to work covariantly.
 */
sealed class Intersection2D<out V> {

    /**
     * List of intersection points (in case of infinite points this is empty).
     */
    open val asList: List<V> = emptyList()

    /**
     * Objects do not intersect.
     */
    object None : Intersection2D<Nothing>()

    /**
     * Objects intersect in a single [point].
     */
    data class SinglePoint<P : Vector2D<P>>(val point: P) : Intersection2D<P>() {
        override val asList = listOf(point)
    }

    /**
     * Objects intersect in a discrete number of [points].
     */
    data class MultiplePoints<P : Vector2D<P>>(val points: List<P>) : Intersection2D<P>() {
        override val asList: List<P> get() = points
    }

    /**
     * Objects intersect in infinite points (e.g. overlapping segments).
     */
    object InfinitePoints : Intersection2D<Nothing>()

    companion object {

        /**
         * @returns the correct intersection object depending of the number of items in the list.
         */
        fun <P : Vector2D<P>> create(points: List<P>): Intersection2D<P> = when {
            points.isEmpty() -> None
            points.size == 1 -> SinglePoint(points.first())
            else -> MultiplePoints(points)
        }
    }
}
