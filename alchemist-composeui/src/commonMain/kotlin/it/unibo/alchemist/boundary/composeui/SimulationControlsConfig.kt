/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui

/**
 * Limits and defaults of the simulation controls shown in the UI.
 */
object SimulationControlsConfig {
    /**
     * Lowest refresh rate, in frames per second, the UI can be set to.
     */
    const val MIN_UI_FPS = 5

    /**
     * Refresh rate, in frames per second, used when none is configured.
     */
    const val DEFAULT_UI_FPS = 30

    /**
     * Highest refresh rate, in frames per second, used when the refresh rate of the display cannot be detected.
     */
    const val DEFAULT_MAX_UI_FPS = 60

    /**
     * Number of decimal digits used to display the simulation time.
     */
    const val DISPLAYED_TIME_DECIMALS = 2
}
