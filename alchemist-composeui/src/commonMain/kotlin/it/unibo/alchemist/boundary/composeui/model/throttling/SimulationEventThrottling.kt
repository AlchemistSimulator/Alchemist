/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model.throttling

import it.unibo.alchemist.boundary.composeui.util.DisplayValues.formatCompact
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Limit on how many simulation events per second the simulation thread may execute.
 *
 * @property value the maximum number of events per second.
 */
internal sealed class SimulationEventThrottling(val value: Int) {
    init {
        require(value >= MIN_SIMULATION_EVENTS_PER_SECOND) {
            "Simulation events per second cannot be less than $MIN_SIMULATION_EVENTS_PER_SECOND, but was $value."
        }
    }

    /** Where this rate sits on the logarithmic speed slider, from 0 to [MAX_SLIDER_POSITION]. */
    val sliderPosition: Float get() = log10(value.coerceAtMost(MAX_SIMULATION_EVENTS_PER_SECOND).toFloat())

    /**
     * Returns the throttling policy for the newly selected event rate [newValue].
     */
    fun update(newValue: Int): SimulationEventThrottling = toEventThrottling(newValue)

    /**
     * Returns the label displayed for this policy.
     */
    fun toLabel(): String = when (this) {
        is FullThrottle -> "Max"
        is EventsPerSecond -> "${value.toLong().formatCompact()} events/s"
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
        const val MAX_SIMULATION_EVENTS_PER_SECOND: Int = 1_000_000

        /** The top of the logarithmic speed slider: the base-10 logarithm of the highest rate. */
        val MAX_SLIDER_POSITION: Float = log10(MAX_SIMULATION_EVENTS_PER_SECOND.toFloat())

        /**
         * The event rate at [position] on the logarithmic speed slider, rounded to two significant digits.
         */
        fun rateAt(position: Float): Int {
            val raw = 10.0.pow(position.toDouble())
            val unit = 10.0.pow(floor(log10(raw)) - 1).coerceAtLeast(1.0)
            return ((raw / unit).roundToInt() * unit).roundToInt()
                .coerceIn(MIN_SIMULATION_EVENTS_PER_SECOND, MAX_SIMULATION_EVENTS_PER_SECOND)
        }

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
}
