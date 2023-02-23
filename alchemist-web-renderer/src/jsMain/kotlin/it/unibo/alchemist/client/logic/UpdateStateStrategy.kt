/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client.logic

/**
 * The strategy used to update the application state. This class is mainly used to specify how the application
 * should behave based on the state of the [it.unibo.alchemist.common.model.RenderMode].
 */
interface UpdateStateStrategy {

    /**
     * Update the application state, the client will do most of the computation.
     */
    suspend fun clientComputation()

    /**
     * Update the application state, the server will do most of the computation.
     */
    suspend fun serverComputation()

    /**
     * Retrieve the simulation status from the simulation and update the application state.
     */
    suspend fun retrieveSimulationStatus()
}
