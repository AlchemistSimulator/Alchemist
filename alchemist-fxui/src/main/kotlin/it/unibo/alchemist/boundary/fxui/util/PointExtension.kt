/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.util

import javafx.scene.shape.Rectangle
import java.awt.Point
import kotlin.math.abs
import kotlin.math.min

/**
 * Utilities for [Point].
 */
object PointExtension {
    /**
     * Creates a [Point].
     *
     * @param x the x coordinate.
     * @param y the y coordinate.
     */
    fun makePoint(x: Number, y: Number) = Point(x.toInt(), y.toInt())

    /**
     * Sums [this] and the given [Point].
     *
     * @param p the other point.
     */
    operator fun Point.plus(p: Point): Point = Point(x + p.x, y + p.y)

    /**
     * Subtracts [this] and the given [Point].
     *
     * @param p the other point.
     */
    operator fun Point.minus(p: Point): Point = Point(x - p.x, y - p.y)

    /**
     * Creates a rectangle that has [this] and [other] as its opposite-diagonal vertexes.
     *
     * @param other the other vertex.
     */
    fun Point.makeRectangleWith(other: Point): Rectangle = Rectangle(
        min(this.x, other.x).toDouble(),
        min(this.y, other.y).toDouble(),
        abs(this.x - other.x).toDouble(),
        abs(this.y - other.y).toDouble(),
    )
}
