/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model.throttling

/**
 * Limit on how many simulation events per second the simulation thread may execute.
 *
 * @property value the maximum number of events per second.
 */
sealed class SimulationEventThrottling(val value: Int) {
    init {
        require(value >= MIN_SIMULATION_EVENTS_PER_SECOND) {
            "Simulation events per second cannot be less than $MIN_SIMULATION_EVENTS_PER_SECOND, but was $value."
        }
    }

    /**
     * Bounds of the event rate and conversion from the rate selected in the UI.
     */
    companion object {
        /**
         * Lowest selectable event rate.
         */
        const val MIN_SIMULATION_EVENTS_PER_SECOND: Int = 1

        /**
         * Highest selectable event rate: selecting it disables throttling.
         */
        const val MAX_SIMULATION_EVENTS_PER_SECOND: Int = 2000

        /**
         * Converts the selected event rate [value] into a throttling policy:
         * [MAX_SIMULATION_EVENTS_PER_SECOND] or more disables throttling.
         */
        fun toEventThrottling(value: Int): SimulationEventThrottling = when {
            value >= MAX_SIMULATION_EVENTS_PER_SECOND -> FullThrottle
            value in MIN_SIMULATION_EVENTS_PER_SECOND..<MAX_SIMULATION_EVENTS_PER_SECOND -> EventsPerSecond(value)
            else ->
                error(
                    "Simulation events per second must be at least $MIN_SIMULATION_EVENTS_PER_SECOND, but was $value.",
                )
        }
    }

    /**
     * Returns the throttling policy for the newly selected event rate [newValue].
     */
    fun update(newValue: Int): SimulationEventThrottling = toEventThrottling(newValue)

    /**
     * Returns the label displayed for this policy.
     */
    fun toLabel(): String = when (this) {
        is FullThrottle -> "Max"
        is EventsPerSecond -> "$value"
    }
}
