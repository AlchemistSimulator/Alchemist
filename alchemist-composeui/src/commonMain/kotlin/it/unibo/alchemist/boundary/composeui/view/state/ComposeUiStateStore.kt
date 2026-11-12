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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the UI state; safe to use from any thread.
 * The UI reads [stateFlow]; all changes go through [set] or [update].
 */
class ComposeUiStateStore(initialState: AlchemistUiState) {
    private val mutableState = MutableStateFlow(initialState)

    /** The current state. */
    val state: AlchemistUiState get() = mutableState.value

    /** The state, observable. */
    val stateFlow: StateFlow<AlchemistUiState> = mutableState.asStateFlow()

    /**
     * Replace the current state.
     */
    fun set(newState: AlchemistUiState) {
        mutableState.value = newState
    }

    /**
     * Mutate the current state atomically.
     */
    fun update(transform: (AlchemistUiState) -> AlchemistUiState) {
        mutableState.update(transform)
    }
}
