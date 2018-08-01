/*
 * Copyright (C) 2010-2018, Danilo Pianini and contributors listed in the main
 * project's alchemist/build.gradle file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception, as described in the file
 * LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.interfaces

import it.unibo.alchemist.model.interfaces.Concentration
import it.unibo.alchemist.model.interfaces.Position2D

/**
 * [OutputMonitor] that handles the graphical part of a bidimensional zoomable simulation in JavaFX.
 *
 * @param <T> the [Concentration] type
</T> */
interface FX2DOutputMonitor<T> : FXOutputMonitor<T, Position2D<*>> {
    /**
     * @param center the point where to zoom
     * @param zoomLevel the desired zoom level
     */
    fun zoomTo(center: Position2D<*>, zoomLevel: Double)
}
