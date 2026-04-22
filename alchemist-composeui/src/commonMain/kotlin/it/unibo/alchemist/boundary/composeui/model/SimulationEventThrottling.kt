/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.model

sealed class SimulationEventThrottling(val value: Int) {
    init {
        require(value >= MIN_SIMULATION_EVENTS_PER_SECOND) {
            "Simulation events per second cannot be less than $MIN_SIMULATION_EVENTS_PER_SECOND, but was $value."
        }
    }

    companion object {
        const val MIN_SIMULATION_EVENTS_PER_SECOND: Int = 1
        const val MAX_SIMULATION_EVENTS_PER_SECOND: Int = 2000
        fun toEventThrottling(value: Int): SimulationEventThrottling = when {
            value >= MAX_SIMULATION_EVENTS_PER_SECOND -> FullThrottle
            value in MIN_SIMULATION_EVENTS_PER_SECOND..<MAX_SIMULATION_EVENTS_PER_SECOND -> EventsPerSecond(value)
            else ->
                error("Simulation events per second must be at least $MIN_SIMULATION_EVENTS_PER_SECOND, but was $value.")
        }
    }

    fun update(newValue: Int): SimulationEventThrottling = toEventThrottling(newValue)

    fun toLabel(): String = when (this) {
        is FullThrottle -> "Max"
        is EventsPerSecond -> "$value"
    }
}
class EventsPerSecond(eventsPerSecond: Int) : SimulationEventThrottling(eventsPerSecond)
data object FullThrottle : SimulationEventThrottling(Int.MAX_VALUE)
