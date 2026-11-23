/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.state.edits

import it.unibo.alchemist.boundary.composeui.view.state.ComposeUiStateStore

/** The frame rate cap of the UI in [store]. */
internal class FrameRateEdits(private val store: ComposeUiStateStore) {
    /** Applies the frame rate in the FPS box, or explains why it cannot. */
    fun apply() = store.update { state ->
        state.controls.fpsInput.toIntOrNull()
            ?.let { state.copy(controls = state.controls.withUiFps(it)) }
            ?: state.withDialog("Invalid FPS", "Insert a valid integer FPS value.")
    }

    /** The FPS box changed to [value], applied at once if it is an allowed frame rate. */
    fun setInput(value: String) = store.update { it.copy(controls = it.controls.withFpsInput(value)) }
}
