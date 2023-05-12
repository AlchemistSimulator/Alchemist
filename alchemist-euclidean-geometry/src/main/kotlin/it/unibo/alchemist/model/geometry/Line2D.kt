/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.model.geometry

/**
 * Defines a straight line in a cartesian plane.
 */
interface Line2D<P : Vector2D<P>> {

    /**
     * Indicates if the line is aligned to the x-axis.
     */
    val isHorizontal: Boolean

    /**
     * Indicates if the line is aligned to the y-axis.
     */
    val isVertical: Boolean

    /**
     * The slope of the line, if [isVertical] this is undefined (= [Double.NaN]).
     */
    val slope: Double

    /**
     * The y-coordinate of the y-intercept (= the point where the line intersects the y-axis). If [isVertical] there's
     * no y-intercept and this is [Double.NaN]. The slope-intercept representation (y = mx + b) uses this intercept.
     */
    val yIntercept: Double

    /**
     * The x-coordinate of the x-intercept (= the point where the line intersects the x-axis). If [isHorizontal]
     * there's no x-intercept and this is [Double.NaN].
     */
    val xIntercept: Double

    /**
     * Checks if the [point] belongs to this line.
     */
    fun contains(point: P): Boolean

    /**
     * Finds the point belonging to the line with the given [x]-coordinate. Throws an [UnsupportedOperationException]
     * if the line [isVertical].
     */
    fun findPoint(x: Double): P

    /**
     * Checks if two lines are parallel.
     */
    fun isParallelTo(other: Line2D<P>): Boolean

    /**
     * Intersects two lines.
     */
    fun intersect(other: Line2D<P>): Intersection2D<P>

    /**
     * Intersects a line and a circle.
     */
    fun intersectCircle(center: P, radius: Double): Intersection2D<P>
}
