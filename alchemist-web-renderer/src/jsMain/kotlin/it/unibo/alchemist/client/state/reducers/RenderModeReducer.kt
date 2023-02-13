/*
 * Copyright (C) 2010-2022, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.client.state.reducers

import it.unibo.alchemist.common.model.RenderMode
import it.unibo.alchemist.client.state.actions.SetRenderMode

/**
 * Reducer for the render mode.
 * @param state the current render mode state.
 * @param action the action to perform.
 * @return the new render mode.
 */
fun renderModeReducer(state: RenderMode, action: Any): RenderMode = when (action) {
    is SetRenderMode -> action.renderMode
    else -> state
}
