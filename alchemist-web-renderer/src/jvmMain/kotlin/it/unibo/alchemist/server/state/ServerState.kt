/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.server.state

import it.unibo.alchemist.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.common.model.surrogate.PositionSurrogate
import it.unibo.alchemist.common.state.CommonState
import it.unibo.alchemist.core.interfaces.Simulation
import it.unibo.alchemist.server.state.reducers.environmentSurrogateReducer
import it.unibo.alchemist.server.state.reducers.simulationReducer

/**
 * The state of the server.
 * The [ServerState] is managed using the
 * <a href="https://reduxkotlin.org/introduction/core-concepts">Core concepts of the ReduxKotlin library</a>.
 * Like in the original Redux library the state is stored in a single class that contains other objects via composition.
 * The state can be changed using actions that must be defined in advance.
 * The unique store that encapsulate the [ServerState] is {@link it.unibo.alchemist.server.state.ServerStore}.
 * @param simulation the simulation. Defaults to null.
 * @param environmentSurrogate the current environment surrogate. Defaults to an uninitializedEnvironment.
 * @see <a href="https://reduxkotlin.org/">ReduxKotlin Documentation</a>
 */
data class ServerState(
    val simulation: Simulation<Any, Nothing>? = null,
    val environmentSurrogate: EnvironmentSurrogate<Any, PositionSurrogate> =
        EnvironmentSurrogate.uninitializedEnvironment(),
) : CommonState()

/**
 * Root reducer of the server. Uses all the other server reducers.
 * @param state the old server state.
 * @param action the action to be applied.
 */
fun rootReducer(
    state: ServerState,
    action: Any,
): ServerState = ServerState(
    simulation = simulationReducer(state.simulation, action),
    environmentSurrogate = environmentSurrogateReducer(state.environmentSurrogate, action),
)
