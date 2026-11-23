/*
 * Copyright (C) 2010-2026, Danilo Pianini and contributors
 * listed, for each module, in the respective subproject's build.gradle.kts file.
 *
 * This file is part of Alchemist, and is distributed under the terms of the
 * GNU General Public License, with a linking exception,
 * as described in the file LICENSE in the Alchemist distribution's top directory.
 */
package it.unibo.alchemist.boundary.composeui.view.state

import it.unibo.alchemist.boundary.composeui.view.state.edits.DialogEdits
import it.unibo.alchemist.boundary.composeui.view.state.edits.FrameRateEdits
import it.unibo.alchemist.boundary.composeui.view.state.edits.JumpEdits
import it.unibo.alchemist.boundary.composeui.view.state.edits.SelectionEdits

/**
 * Changes that only touch the UI state in [store], by area, the same whatever runs the simulation.
 */
internal class UiEdits(store: ComposeUiStateStore) {
    /** The "to time" and "to step" boxes. */
    val jumps: JumpEdits = JumpEdits(store)

    /** The frame rate cap of the UI. */
    val frameRate: FrameRateEdits = FrameRateEdits(store)

    /** The selected nodes, the inspector and the value editor. */
    val selection: SelectionEdits = SelectionEdits(store)

    /** The error dialog. */
    val dialogs: DialogEdits = DialogEdits(store)
}
