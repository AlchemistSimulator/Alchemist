/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.implementations.geometry.euclidean2d

import it.unibo.alchemist.model.interfaces.geometry.Vector2D
import it.unibo.alchemist.model.interfaces.geometry.euclidean2d.Segment2D

/**
 * Describes the result an intersection operation in an euclidean 2D space.
 * Type [V] must extend [Vector2D].
 * The requirement is not explicitly enforced to allow the class to work covariantly.
 */
sealed class Intersection2D<out V> {
    /**
     * Objects do not intersect.
     */
    object None : Intersection2D<Nothing>()
    /**
     * Objects intersect in a single [point].
     */
    data class SinglePoint<P : Vector2D<P>>(val point: P) : Intersection2D<P>()
    /**
     * Objects intersect in multiple [points].
     */
    data class MultiplePoints<P : Vector2D<P>>(val points: List<P>) : Intersection2D<P>()
    /**
     * Objects intersect in a [segment].
     */
    data class Segment<P : Vector2D<P>>(val segment: Segment2D<P>) : Intersection2D<P>()
    /**
     * Objects intersect as a line.
     */
    object Line : Intersection2D<Nothing>()
}
