/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client.state.reducers

import it.unibo.alchemist.client.state.actions.SetStatusSurrogate
import it.unibo.alchemist.common.model.surrogate.StatusSurrogate

/**
 * Reducer for the [StatusSurrogate].
 * @param state the current [StatusSurrogate].
 * @param action the action to perform.
 * @return the new [StatusSurrogate].
 */
fun statusSurrogateReducer(state: StatusSurrogate, action: Any): StatusSurrogate = when (action) {
    is SetStatusSurrogate -> action.statusSurrogate
    else -> state
}
