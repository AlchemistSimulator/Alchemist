/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.view.state

import it.unibo.alchemist.boundary.composeui.model.AlchemistUiState
import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks

/**
 * The UI state and the actions the user can trigger.
 *
 * @property store the UI state.
 * @property callbacks what runs when the user acts.
 */
internal data class ComposeUiController(val store: ComposeUiStateStore, val callbacks: AlchemistUiCallbacks) {
    /** Starts a store from [initialState] and builds the [callbacks] on it. */
    constructor(
        initialState: AlchemistUiState,
        callbacks: (ComposeUiStateStore) -> AlchemistUiCallbacks,
    ) : this(ComposeUiStateStore(initialState), callbacks)

    private constructor(store: ComposeUiStateStore, callbacks: (ComposeUiStateStore) -> AlchemistUiCallbacks) :
        this(store, callbacks(store))
}
