/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model.pacing

/**
 * How the simulation speed is limited.
 *
 * @property label the name shown on the mode button.
 * @property description what the mode does, for screen readers and tooltips.
 * @property defaultRate the rate used when the mode is selected.
 */
internal enum class PacingMode(val label: String, val description: String, val defaultRate: Double) {
    /** No limit. */
    MAX_SPEED("max", "Run as fast as possible", 1.0),

    /** At most a given number of events per second. */
    EVENTS_PER_SECOND("events/s", "Run at most this many events per second", 1000.0),

    /** A given amount of simulated time per second: 1 is real time. */
    TIME_PER_SECOND("time/s", "Run this many simulated time units per second", 1.0),
}
