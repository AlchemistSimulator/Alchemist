/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.fxui.interaction.util

import it.unibo.alchemist.boundary.fxui.util.PointExtension.minus
import it.unibo.alchemist.boundary.fxui.util.PointExtension.plus
import java.awt.Point

/**
 * Helps manage panning done through the mouse,
 * therefore analog in the sense that the
 * panning can go towards any direction.
 * @param current the current position of the mouse.
 */
class AnalogPanHelper(private var current: Point) {
    /**
     * Returns whether this [AnalogPanHelper] is still valid.
     * Invalidation happens when [close] is called, for example when the mouse goes out of bounds.
     */
    var valid: Boolean = true
        private set

    /**
     * Updates the panning position and returns it.
     * @param next the destination point
     * @param view the position of the view
     */
    fun update(next: Point, view: Point): Point {
        check(valid) {
            "Unable to pan after finalizing the PanHelper"
        }
        return (view + next - current).also { current = next }
    }

    /**
     * Closes the helper. This invalidates the [AnalogPanHelper]
     */
    fun close() {
        valid = false
    }
}
