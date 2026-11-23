/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */

package it.unibo.alchemist.boundary.composeui.demo

import it.unibo.alchemist.boundary.composeui.model.callbacks.AlchemistUiCallbacks
import it.unibo.alchemist.boundary.composeui.model.pacing.PacingMode
import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore

/**
 * The speed limit of the demo scene, which only records it.
 */
internal class DemoPace(private val store: ComposeUiStateStore) : AlchemistUiCallbacks.Pace {
    override suspend fun setMode(mode: PacingMode) = store.update {
        it.copy(controls = it.controls.withPacingMode(mode))
    }

    override suspend fun setRateInput(value: String) = store.update {
        it.copy(controls = it.controls.withPacingInput(value))
    }
}
