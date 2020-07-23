/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary

import java.awt.Point

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
