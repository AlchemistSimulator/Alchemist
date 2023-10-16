/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.graphql.util

import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.EnvironmentSurrogate
import it.unibo.alchemist.boundary.graphql.schema.model.surrogates.toGraphQLEnvironmentSurrogate
import it.unibo.alchemist.core.Simulation
import it.unibo.alchemist.model.Environment
import it.unibo.alchemist.model.Position
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * A simple event bus for [Environment]s.
 * @param simulation the simulation to which the environments belong
 */
class EnvironmentEventBus<T, P : Position<out P>>(private val simulation: Simulation<T, P>) {
    private val environments: Channel<EnvironmentSurrogate<T, P>> =
        Channel(onBufferOverflow = BufferOverflow.DROP_OLDEST)

    /**
     * Adds an environment to the bus.
     * @param environment the environment to add to the bus
     */
    fun sendEnvironment(environment: Environment<T, P>) {
        environments.trySend(environment.toGraphQLEnvironmentSurrogate(simulation))
    }

    /**
     * Works only for single subscriber due to channel's elements consumption when receiving.
     * @return a [kotlinx.coroutines.flow.Flow] of [EnvironmentSurrogate]s.
     */
    fun asFlow() = environments.receiveAsFlow()
}
