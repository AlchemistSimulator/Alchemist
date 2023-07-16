/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction

import java.awt.Point
import kotlin.math.max
import kotlin.math.min

/**
 * Cardinal and intercardinal directions indicating a movement.
 * @param x the X-value. Grows positively towards the "right".
 * @param y the Y-value. Grows positively upwards.
 */
enum class Direction2D(val x: Int, val y: Int) {
    NONE(0, 0),
    NORTH(0, 1),
    SOUTH(0, -1),
    EAST(1, 0),
    WEST(-1, 0),
    NORTHEAST(1, 1),
    SOUTHEAST(1, -1),
    SOUTHWEST(-1, -1),
    NORTHWEST(-1, 1),
    ;

    private fun flip(xFlip: Boolean = true, yFlip: Boolean = true): Direction2D =
        values().find {
            it.x == (if (xFlip) -x else x) && it.y == if (yFlip) -y else y
        } ?: NONE

    /**
     * Flips the direction horizontally and vertically.
     */
    val flipped: Direction2D
        get() = flip()

    /**
     * Flips the direction's X-values.
     */
    val flippedX: Direction2D
        get() = flip(yFlip = false)

    /**
     * Flips the direction's Y-values.
     */
    val flippedY: Direction2D
        get() = flip(xFlip = false)

    private fun Int.limited(): Int = min(1, max(this, -1))

    /**
     * Sums with a direction.
     */
    operator fun plus(other: Direction2D): Direction2D =
        values().find {
            it.x == (x + other.x).limited() && it.y == (y + other.y).limited()
        } ?: NONE

    /**
     * Subtracts with a direction.
     */
    operator fun minus(other: Direction2D): Direction2D =
        values().find {
            it.x == (x - other.x).limited() && it.y == (y - other.y).limited()
        } ?: NONE

    /**
     * Multiplies by a scalar.
     */
    operator fun times(scalar: Int): Point = Point(x * scalar, y * scalar)

    /**
     * Returns whether this direction contains [other].
     * Specifically, a direction "D" contains another if D [Direction2D.plus] [other] equals D.
     * For example, [NORTHEAST] contains [NORTH] and [EAST], but not [WEST].
     * All directions contain [NONE]. [NONE] contains only [NONE].
     */
    operator fun contains(other: Direction2D): Boolean {
        return this + other == this
    }
}
