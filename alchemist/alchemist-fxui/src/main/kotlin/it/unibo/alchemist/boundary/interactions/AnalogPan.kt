/*
 * Copyright (C) 2010-2020, Danilo Pianini and contributors
 * listed in the main project's alchemist/build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.interactions

import it.unibo.alchemist.boundary.minus
import it.unibo.alchemist.boundary.plus
import java.awt.Point

/**
 * Manages panning.
 */
class AnalogPan(private var current: Point) {
    /**
     * Returns whether this [AnalogPan] is still valid.
     * Invalidation happens when [close] is called, for example when the mouse goes out of bounds.
     */
    var valid: Boolean = true
        private set

    /**
     * Updates the panning position and returns it.
     * @param next the destination point
     * @param view the position of the view
     */
    fun update(next: Point, view: Point): Point = if (valid) {
        (view + next - current).also { current = next }
    } else {
        throw IllegalStateException("Unable to pan after finalizing the PanHelper")
    }

    /**
     * Closes the helper. This invalidates the [AnalogPan]
     */
    fun close() {
        valid = false
    }
}
