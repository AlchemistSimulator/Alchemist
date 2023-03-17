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
 * The Alchemist simulation adapter that allows MultiVesta adapter
 * to interact with the simulation.
 */
interface AlchemistSimulationAdapter {

    /**
     * Get the simulation time.
     */
    fun getTime(): Double

    /**
     * Get the value for the given observation.
     * @param obs the name of the observation
     */
    fun rval(obs: String): Double

    /**
     * Get the value for the given observation.
     * @param obsId the id of the observation
     */
    fun rval(obsId: Int): Double

    /**
     * Perform a simulation step. In Alchemist this means that the simulation
     * will be advanced of the number of step needed to reach the next time.
     */
    fun doStep()

    /**
     * Perform the whole simulation.
     */
    fun performWholeSimulation()
}
