/*
 * Copyright (C) 2010-2023, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.webui.server.state.reducers

import it.unibo.alchemist.boundary.webui.common.model.surrogate.EnvironmentSurrogate
import it.unibo.alchemist.boundary.webui.common.model.surrogate.PositionSurrogate
import it.unibo.alchemist.boundary.webui.server.state.actions.SetEnvironmentSurrogate

/**
 * Reducer for the environment surrogate.
 * @param state the current state.
 * @param action the requested action.
 */
fun environmentSurrogateReducer(
    state: EnvironmentSurrogate<Any, PositionSurrogate>,
    action: Any,
): EnvironmentSurrogate<Any, PositionSurrogate> =
    when (action) {
        is SetEnvironmentSurrogate<*, *> -> action.environmentSurrogate
        else -> state
    }
