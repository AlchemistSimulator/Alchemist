/*
 * Copyright (C) 2010-2024, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package stores.reducers

import stores.actions.ScaleTranslateAction
import stores.states.ScaleTranslateState

/**
 * Reduces the current state of scale and translation based on the provided action.
 * @param state The current state of scale and translation.
 * @param action The action to apply to the state.
 * @return The new state of scale and translation after applying the action.
 */
fun scaleTranslateReducer(state: ScaleTranslateState, action: ScaleTranslateAction): ScaleTranslateState {
    return when (action) {
        is ScaleTranslateAction.SetScale -> {
            state.copy(scale = action.scale)
        }

        is ScaleTranslateAction.SetTranslation -> {
            state.copy(translate = action.translate)
        }
    }
}
