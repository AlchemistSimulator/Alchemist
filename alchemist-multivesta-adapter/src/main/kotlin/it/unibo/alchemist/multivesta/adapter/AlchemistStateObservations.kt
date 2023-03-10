/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.multivesta.adapter

/**
 * Represents the state of an Alchemist simulation at a given time.
 * @param time the time at which the state is referred to.
 */
class AlchemistStateObservations(val time: Double) {
    private val observations: MutableMap<String, Double> = mutableMapOf()

    /**
     * Adds an observation for the state.
     * @param name the name of the observation
     * @param value the value for such observation
     */
    fun addObservation(name: String, value: Double) {
        observations[name] = value
    }

    /**
     * Returns the value of the observation with the given name.
     */
    fun getObservation(name: String): Double = when (name) {
        "time", "step" -> time
        else -> observations[name] ?: throw IllegalArgumentException("Observation $name not found")
    }

    /**
     * Returns the value of the observation with the given id.
     */
    fun getObservation(id: Int): Double {
        return observations.values.elementAt(id)
    }
}
