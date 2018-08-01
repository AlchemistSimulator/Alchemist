/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.monitors

import it.unibo.alchemist.boundary.interfaces.FX2DOutputMonitor
import it.unibo.alchemist.model.interfaces.Concentration
import it.unibo.alchemist.model.interfaces.Position2D

/**
 * Simple implementation of a monitor that graphically represents a simulation on a 2D map.
 *
 * @param <T> The type which describes the [Concentration] of a molecule
</T> */
class FXMapDisplay<T>
@JvmOverloads constructor(step: Int = DEFAULT_NUMBER_OF_STEPS) : AbstractFXDisplay<T>(step), FX2DOutputMonitor<T> {
    // TODO
    // TODO
    // TODO
    // TODO

    override fun zoomTo(center: Position2D<*>, zoomLevel: Double) {
        assert(center.getDimensions() == 2)
        val wh = wormhole
        wh?.zoomOnPoint(wh.getViewPoint(center), zoomLevel)
    }

    companion object {
        /**
         * Default serial version UID.
         */
        private val serialVersionUID = 1L
    }
}
