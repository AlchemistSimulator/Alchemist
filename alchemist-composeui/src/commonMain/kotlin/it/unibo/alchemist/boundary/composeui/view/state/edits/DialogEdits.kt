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

/** The error dialog in [store]. */
internal class DialogEdits(private val store: ComposeUiStateStore) {
    /** Closes the dialog. */
    fun dismiss() = store.update { it.copy(controls = it.controls.copy(dialog = null)) }

    /** Shows a dialog. */
    fun show(title: String, message: String) = store.update { it.withDialog(title, message) }
}
